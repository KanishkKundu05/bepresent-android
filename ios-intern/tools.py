"""MCP tools for the agentic debugger, defined via Agent SDK @tool decorator."""

import os
import json
import subprocess
import base64
from typing import Any

from claude_agent_sdk import tool, create_sdk_mcp_server

from config import APP_PACKAGE, GRADLE_ROOT, SCREENSHOT_DIR
from adb_client import (
    run_arbitrary, take_screenshot as adb_screenshot,
    logcat_dump, logcat_clear,
)


# ── Tool Definitions ─────────────────────────────────────────────────

@tool(
    "run_adb",
    "Execute an ADB command on the Android emulator. "
    "Can run shell commands, install APKs, simulate input, etc. "
    "The 'adb' prefix is optional. "
    "Examples: 'shell input tap 500 1000', 'shell am start -n com.app/.Activity', "
    "'shell dumpsys activity activities'",
    {"command": str},
)
async def run_adb(args: dict[str, Any]) -> dict[str, Any]:
    output = run_arbitrary(args["command"])
    return {"content": [{"type": "text", "text": output or "(no output)"}]}


@tool(
    "edit_source",
    "Edit a Kotlin source file in the project. Provide the file path "
    "relative to the project root (e.g. 'app/src/main/java/com/bepresent/android/service/MonitoringService.kt') "
    "and the complete new file content. The file will be overwritten entirely.",
    {
        "type": "object",
        "properties": {
            "file_path": {
                "type": "string",
                "description": "File path relative to project root",
            },
            "content": {
                "type": "string",
                "description": "The complete new file content",
            },
        },
        "required": ["file_path", "content"],
    },
)
async def edit_source(args: dict[str, Any]) -> dict[str, Any]:
    file_path = os.path.join(GRADLE_ROOT, args["file_path"])
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, "w") as f:
        f.write(args["content"])
    return {
        "content": [
            {"type": "text", "text": f"Wrote {len(args['content'])} chars to {args['file_path']}"}
        ]
    }


@tool(
    "rebuild_app",
    "Build the app with Gradle and reinstall on the emulator. "
    "Runs './gradlew installDebug' (~30-60s). Use after editing source files.",
    {},
)
async def rebuild_app(args: dict[str, Any]) -> dict[str, Any]:
    result = subprocess.run(
        ["./gradlew", "installDebug"],
        cwd=GRADLE_ROOT,
        capture_output=True,
        text=True,
        timeout=300,
    )
    stdout = result.stdout[-2000:] if len(result.stdout) > 2000 else result.stdout
    if result.returncode != 0:
        stderr = result.stderr[-2000:] if len(result.stderr) > 2000 else result.stderr
        return {
            "content": [
                {"type": "text", "text": f"BUILD FAILED (exit {result.returncode}):\n{stderr}\n{stdout}"}
            ],
            "is_error": True,
        }
    return {"content": [{"type": "text", "text": f"BUILD SUCCESS\n{stdout[-500:]}"}]}


@tool(
    "take_screenshot",
    "Capture the current emulator screen. Returns a PNG screenshot. "
    "Use to verify UI state, check if shield is showing, etc.",
    {
        "type": "object",
        "properties": {
            "label": {
                "type": "string",
                "description": "Short label for the file (e.g. 'shield_visible')",
            },
        },
        "required": [],
    },
)
async def take_screenshot(args: dict[str, Any]) -> dict[str, Any]:
    label = args.get("label", "screen")
    b64, path = adb_screenshot(label)
    return {
        "content": [
            {
                "type": "image",
                "data": b64,
                "mimeType": "image/png",
            }
        ]
    }


@tool(
    "get_logs",
    "Pull recent logcat output filtered to the app's PID. "
    "Returns the last N lines. Use to diagnose crashes, check service behavior, etc.",
    {
        "type": "object",
        "properties": {
            "max_lines": {
                "type": "integer",
                "description": "Maximum number of log lines to return",
            },
            "clear_after": {
                "type": "boolean",
                "description": "Clear logcat buffer after reading",
            },
        },
        "required": [],
    },
)
async def get_logs(args: dict[str, Any]) -> dict[str, Any]:
    max_lines = args.get("max_lines", 150)
    logs = logcat_dump(max_lines=max_lines)
    if args.get("clear_after", False):
        logcat_clear()
    return {"content": [{"type": "text", "text": logs or "(no logs)"}]}


@tool(
    "mark_resolved",
    "End the debug loop. Call when you have: "
    "(1) diagnosed and fixed the issue, "
    "(2) determined behavior is correct, or "
    "(3) identified root cause but fix requires user input.",
    {
        "type": "object",
        "properties": {
            "summary": {
                "type": "string",
                "description": "Summary of findings",
            },
            "files_changed": {
                "type": "array",
                "items": {"type": "string"},
                "description": "Files modified (if any)",
            },
        },
        "required": ["summary"],
    },
)
async def mark_resolved(args: dict[str, Any]) -> dict[str, Any]:
    return {
        "content": [
            {
                "type": "text",
                "text": json.dumps({
                    "status": "resolved",
                    "summary": args["summary"],
                    "files_changed": args.get("files_changed", []),
                }),
            }
        ]
    }


# ── MCP Server ───────────────────────────────────────────────────────

def create_debugger_server():
    """Create the in-process MCP server with all debugger tools."""
    return create_sdk_mcp_server(
        name="debugger",
        version="1.0.0",
        tools=[run_adb, edit_source, rebuild_app, take_screenshot, get_logs, mark_resolved],
    )
