"""ADB wrapper for emulator interaction."""

import base64
import subprocess
import shutil
import tempfile
import os
from typing import Optional

from config import APP_PACKAGE, SCREENSHOT_DIR


def _run(args: list[str], timeout: int = 30) -> subprocess.CompletedProcess:
    """Run a command and return the result."""
    return subprocess.run(args, capture_output=True, text=True, timeout=timeout)


def _adb(*args: str, timeout: int = 30) -> str:
    """Run an adb command and return stdout."""
    result = _run(["adb", *args], timeout=timeout)
    if result.returncode != 0 and result.stderr:
        return f"[adb error] {result.stderr.strip()}\n{result.stdout.strip()}"
    return result.stdout.strip()


# ── Device Management ────────────────────────────────────────────────

def wait_for_device(timeout: int = 60) -> str:
    return _adb("wait-for-device", timeout=timeout)


def get_device_state() -> str:
    return _adb("get-state")


def is_device_ready() -> bool:
    state = get_device_state()
    if state != "device":
        return False
    boot = _adb("shell", "getprop", "sys.boot_completed")
    return boot.strip() == "1"


# ── App Lifecycle ────────────────────────────────────────────────────

def install_apk(apk_path: str) -> str:
    return _adb("install", "-r", "-t", apk_path, timeout=120)


def launch_app() -> str:
    return _adb("shell", "am", "start", "-n",
                f"{APP_PACKAGE}/.MainActivity")


def force_stop_app() -> str:
    return _adb("shell", "am", "force-stop", APP_PACKAGE)


def get_app_pid() -> Optional[int]:
    output = _adb("shell", "pidof", APP_PACKAGE)
    try:
        return int(output.strip())
    except (ValueError, TypeError):
        return None


# ── Logs ─────────────────────────────────────────────────────────────

def logcat_dump(since: Optional[str] = None, max_lines: int = 200) -> str:
    """Pull recent logcat filtered to our app's PID."""
    pid = get_app_pid()
    if pid:
        args = ["shell", "logcat", "-d", "--pid", str(pid), "-v", "time"]
    else:
        args = ["shell", "logcat", "-d", "-v", "time"]

    if since:
        args.extend(["-T", since])

    output = _adb(*args)
    lines = output.splitlines()
    if len(lines) > max_lines:
        lines = lines[-max_lines:]
    return "\n".join(lines)


def logcat_clear() -> str:
    return _adb("shell", "logcat", "-c")


# ── Screenshots ──────────────────────────────────────────────────────

def take_screenshot(label: str = "screen") -> tuple[str, str]:
    """Capture screenshot, return (base64_data, local_path)."""
    os.makedirs(SCREENSHOT_DIR, exist_ok=True)

    remote_path = "/sdcard/screenshot.png"
    _adb("shell", "screencap", "-p", remote_path)

    local_path = os.path.join(SCREENSHOT_DIR, f"{label}.png")
    _adb("pull", remote_path, local_path)
    _adb("shell", "rm", remote_path)

    with open(local_path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("utf-8")

    return b64, local_path


# ── Input Events ─────────────────────────────────────────────────────

def tap(x: int, y: int) -> str:
    return _adb("shell", "input", "tap", str(x), str(y))


def swipe(x1: int, y1: int, x2: int, y2: int, duration_ms: int = 300) -> str:
    return _adb("shell", "input", "swipe",
                str(x1), str(y1), str(x2), str(y2), str(duration_ms))


def press_home() -> str:
    return _adb("shell", "input", "keyevent", "KEYCODE_HOME")


def press_back() -> str:
    return _adb("shell", "input", "keyevent", "KEYCODE_BACK")


def input_text(text: str) -> str:
    return _adb("shell", "input", "text", text)


# ── System State ─────────────────────────────────────────────────────

def dumpsys_activity_top() -> str:
    """Get the current top activity."""
    output = _adb("shell", "dumpsys", "activity", "top")
    # Only return the relevant portion
    lines = output.splitlines()
    relevant = []
    for line in lines:
        if "ACTIVITY" in line or "TaskRecord" in line or "mResumedActivity" in line:
            relevant.append(line.strip())
    return "\n".join(relevant[-10:]) if relevant else output[-500:]


def dumpsys_usagestats() -> str:
    """Get usage stats summary."""
    output = _adb("shell", "dumpsys", "usagestats")
    lines = output.splitlines()
    return "\n".join(lines[:50])  # first 50 lines is enough


def get_foreground_activity() -> str:
    """Get the current foreground activity name."""
    output = _adb("shell", "dumpsys", "activity", "activities")
    for line in output.splitlines():
        if "mResumedActivity" in line or "topResumedActivity" in line:
            return line.strip()
    return "(unknown)"


def launch_package(package: str) -> str:
    """Launch an arbitrary package via monkey."""
    return _adb("shell", "monkey", "-p", package, "-c",
                "android.intent.category.LAUNCHER", "1")


def list_installed_packages() -> list[str]:
    """List third-party installed packages."""
    output = _adb("shell", "pm", "list", "packages", "-3")
    return [line.replace("package:", "").strip()
            for line in output.splitlines() if line.startswith("package:")]


def run_arbitrary(command: str) -> str:
    """Run an arbitrary adb command string."""
    parts = command.split()
    if parts and parts[0] == "adb":
        parts = parts[1:]  # strip leading 'adb' if user included it
    return _adb(*parts, timeout=60)
