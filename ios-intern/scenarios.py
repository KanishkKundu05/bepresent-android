"""Predefined test scenarios for the agentic debugger.

Each scenario is a dict with:
  - name: human-readable label
  - description: what to test
  - setup_steps: ADB commands to run before handing off to Claude
  - initial_prompt: the first message sent to Claude to kick off the loop
"""

from config import APP_PACKAGE

SCENARIOS = {
    "intention_block_flow": {
        "name": "Intention Blocking Flow",
        "description": "Verify that creating an intention blocks the target app and the shield appears.",
        "setup_steps": [
            f"shell am force-stop {APP_PACKAGE}",
            f"shell am start -n {APP_PACKAGE}/.MainActivity",
        ],
        "initial_prompt": (
            "Test the intention blocking flow end-to-end:\n"
            "1. The app should be open on the dashboard. Take a screenshot to verify.\n"
            "2. Check which third-party apps are installed on the emulator.\n"
            "3. Create an intention for one of them (navigate the UI using taps).\n"
            "4. Go home, then launch the blocked app.\n"
            "5. Verify the BlockedAppActivity shield appears within 2 seconds.\n"
            "6. Check logs to confirm MonitoringService detected the foreground app.\n"
            "Report any bugs or unexpected behavior."
        ),
    },

    "shield_bypass_check": {
        "name": "Shield Bypass Check",
        "description": "Attempt to bypass the blocking shield via back button, recents, etc.",
        "setup_steps": [
            f"shell am force-stop {APP_PACKAGE}",
            f"shell am start -n {APP_PACKAGE}/.MainActivity",
        ],
        "initial_prompt": (
            "Test whether the blocking shield can be bypassed:\n"
            "1. First, verify there's an active intention blocking an app (check logs/state).\n"
            "2. Launch the blocked app and wait for the shield.\n"
            "3. Try pressing back — does it go home or back to the blocked app?\n"
            "4. Try pressing recents and switching back to the blocked app.\n"
            "5. Try pressing home, then immediately launching the blocked app again.\n"
            "6. Check for any timing gaps where the blocked app is visible for more than ~1s.\n"
            "Report all bypass vectors found."
        ),
    },

    "session_lifecycle": {
        "name": "Session Lifecycle",
        "description": "Test starting a session, blocking apps, goal reached, and session end.",
        "setup_steps": [
            f"shell am force-stop {APP_PACKAGE}",
            f"shell am start -n {APP_PACKAGE}/.MainActivity",
        ],
        "initial_prompt": (
            "Test the full session lifecycle:\n"
            "1. Take a screenshot to see current dashboard state.\n"
            "2. Start a new session (short duration, e.g., 1 minute) blocking at least one app.\n"
            "3. Verify the monitoring notification appears.\n"
            "4. Try launching a blocked app — verify shield shows with session shield type.\n"
            "5. Wait for the session goal alarm to fire.\n"
            "6. Verify the notification updates to 'Goal Reached'.\n"
            "7. End the session and verify monitoring stops if no intentions are active.\n"
            "Check logs throughout for errors or unexpected state transitions."
        ),
    },

    "permission_flow": {
        "name": "Permission Onboarding",
        "description": "Test the permission granting flow on a fresh install.",
        "setup_steps": [
            f"shell pm clear {APP_PACKAGE}",  # wipe app data
            f"shell am start -n {APP_PACKAGE}/.MainActivity",
        ],
        "initial_prompt": (
            "Test the permission onboarding flow:\n"
            "1. The app was just cleared (fresh state). Take a screenshot.\n"
            "2. The onboarding should appear asking for permissions.\n"
            "3. Check that usage stats permission is correctly detected as missing.\n"
            "4. Navigate through the onboarding flow.\n"
            "5. After permissions are granted, verify the app reaches the dashboard.\n"
            "Report any crashes, ANRs, or confusing UX."
        ),
    },

    "crash_diagnosis": {
        "name": "Crash Diagnosis",
        "description": "Generic scenario — launch the app and investigate any crashes.",
        "setup_steps": [
            f"shell am force-stop {APP_PACKAGE}",
            f"shell logcat -c",  # clear old logs
            f"shell am start -n {APP_PACKAGE}/.MainActivity",
        ],
        "initial_prompt": (
            "The app may be crashing or misbehaving. Investigate:\n"
            "1. Pull logs and check for any exceptions, ANRs, or fatal errors.\n"
            "2. Take a screenshot to see current state.\n"
            "3. If crashed, identify the stack trace and root cause.\n"
            "4. If the app is running, exercise the main flows and check for errors.\n"
            "5. If you find a bug, fix it, rebuild, and verify the fix.\n"
            "Provide a clear diagnosis."
        ),
    },

    "freeform": {
        "name": "Freeform Debug",
        "description": "Open-ended — provide your own prompt at runtime.",
        "setup_steps": [],
        "initial_prompt": "",  # filled in at runtime
    },
}
