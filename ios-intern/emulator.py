"""AVD emulator lifecycle management."""

import subprocess
import time
import os
from typing import Optional

from config import AVD_NAME, AVD_DEVICE, AVD_SYSTEM_IMAGE
from adb_client import is_device_ready


def _run(args: list[str], timeout: int = 120, **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(args, capture_output=True, text=True, timeout=timeout, **kwargs)


def avd_exists() -> bool:
    """Check if our AVD already exists."""
    result = _run(["avdmanager", "list", "avd", "-c"])
    return AVD_NAME in result.stdout


def download_system_image() -> str:
    """Download the system image if not present."""
    result = _run(
        ["sdkmanager", "--install", AVD_SYSTEM_IMAGE],
        timeout=600,
    )
    return result.stdout + result.stderr


def create_avd() -> str:
    """Create the AVD if it doesn't exist."""
    if avd_exists():
        return f"AVD '{AVD_NAME}' already exists."

    download_system_image()

    result = _run([
        "avdmanager", "create", "avd",
        "--name", AVD_NAME,
        "--device", AVD_DEVICE,
        "--package", AVD_SYSTEM_IMAGE,
        "--force",
    ])
    return result.stdout + result.stderr


def start_emulator(headless: bool = False, wipe: bool = False) -> subprocess.Popen:
    """Launch the emulator in the background. Returns the Popen handle."""
    cmd = ["emulator", "-avd", AVD_NAME, "-gpu", "auto"]

    if headless:
        cmd.append("-no-window")
    if wipe:
        cmd.append("-wipe-data")

    # Launch detached
    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    return proc


def wait_for_boot(timeout: int = 180) -> bool:
    """Block until emulator is fully booted or timeout."""
    deadline = time.time() + timeout
    while time.time() < deadline:
        if is_device_ready():
            return True
        time.sleep(3)
    return False


def kill_emulator() -> str:
    """Kill the running emulator."""
    result = _run(["adb", "emu", "kill"])
    return result.stdout + result.stderr


def grant_usage_stats_permission() -> str:
    """Grant usage stats permission via appops (avoids manual Settings navigation)."""
    from config import APP_PACKAGE
    from adb_client import _adb
    return _adb("shell", "appops", "set", APP_PACKAGE,
                "android:get_usage_stats", "allow")


def grant_post_notifications() -> str:
    """Grant POST_NOTIFICATIONS runtime permission."""
    from config import APP_PACKAGE
    from adb_client import _adb
    return _adb("shell", "pm", "grant", APP_PACKAGE,
                "android.permission.POST_NOTIFICATIONS")


def setup_permissions() -> dict[str, str]:
    """Grant all permissions the app needs for testing."""
    return {
        "usage_stats": grant_usage_stats_permission(),
        "notifications": grant_post_notifications(),
    }
