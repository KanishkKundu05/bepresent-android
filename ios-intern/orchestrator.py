"""Agentic debugger orchestrator — powered by Claude Agent SDK.

Usage:
    python orchestrator.py --scenario intention_block_flow
    python orchestrator.py --scenario freeform --prompt "Check why shield flickers"
    python orchestrator.py --list-scenarios
    python orchestrator.py --setup-only
"""

import argparse
import asyncio
import json
import os
import subprocess
import sys
import time
from datetime import datetime

from claude_agent_sdk import (
    query,
    ClaudeAgentOptions,
    HookMatcher,
    AssistantMessage,
    ResultMessage,
)

from config import (
    MAX_TURNS, SYSTEM_PROMPT, LOG_DIR, GRADLE_ROOT, APP_PACKAGE,
)
from adb_client import (
    logcat_clear, get_foreground_activity, get_app_pid,
    logcat_dump, run_arbitrary, is_device_ready,
)
from emulator import (
    create_avd, start_emulator, wait_for_boot,
    setup_permissions,
)
from tools import create_debugger_server
from scenarios import SCENARIOS


def log(msg: str):
    ts = datetime.now().strftime("%H:%M:%S")
    print(f"[{ts}] {msg}")


# ── Hooks ────────────────────────────────────────────────────────────

async def log_tool_calls(input_data, tool_use_id, context):
    """PreToolUse hook: log every tool invocation."""
    tool_name = input_data.get("tool_name", "unknown")
    tool_input = input_data.get("tool_input", {})
    # Strip long content from edit_source for logging
    display_input = {k: (v[:80] + "..." if isinstance(v, str) and len(v) > 80 else v)
                     for k, v in tool_input.items()}
    log(f"  Tool: {tool_name}({json.dumps(display_input, default=str)[:150]})")
    return {}


async def check_resolution(input_data, tool_use_id, context):
    """PostToolUse hook: detect mark_resolved and print summary."""
    tool_name = input_data.get("tool_name", "")
    if tool_name == "mcp__debugger__mark_resolved":
        result = input_data.get("tool_result", "")
        try:
            if isinstance(result, str):
                resolution = json.loads(result)
            elif isinstance(result, list):
                for block in result:
                    if isinstance(block, dict) and block.get("type") == "text":
                        resolution = json.loads(block["text"])
                        break
            else:
                resolution = result

            log(f"\n{'=' * 60}")
            log(f"RESOLVED: {resolution.get('summary', '(no summary)')}")
            files = resolution.get("files_changed", [])
            if files:
                log(f"Files changed: {', '.join(files)}")
            log(f"{'=' * 60}\n")
        except (json.JSONDecodeError, TypeError, KeyError):
            log(f"RESOLVED (raw): {str(result)[:300]}")
    return {}


# ── Scenario Runner ──────────────────────────────────────────────────

def run_setup_steps(steps: list[str]):
    """Execute ADB setup commands for a scenario."""
    for cmd in steps:
        log(f"  setup: adb {cmd}")
        result = run_arbitrary(cmd)
        if result:
            log(f"    -> {result[:200]}")
        time.sleep(1)


def collect_initial_state() -> str:
    """Gather current emulator state for context."""
    return "\n".join([
        f"Foreground: {get_foreground_activity()}",
        f"App PID: {get_app_pid() or 'not running'}",
        f"\n--- Recent Logs ---\n{logcat_dump(max_lines=50)}",
    ])


async def run_loop(scenario_name: str, custom_prompt: str = ""):
    """Run the agentic debug loop for a scenario using Claude Agent SDK."""

    scenario = SCENARIOS.get(scenario_name)
    if not scenario:
        print(f"Unknown scenario: {scenario_name}")
        print(f"Available: {', '.join(SCENARIOS.keys())}")
        sys.exit(1)

    initial_prompt = custom_prompt or scenario["initial_prompt"]
    if not initial_prompt:
        print("Freeform scenario requires --prompt")
        sys.exit(1)

    # ── Verify emulator ──────────────────────────────────────────────
    if not is_device_ready():
        log("Emulator not ready. Waiting...")
        if not wait_for_boot(timeout=120):
            log("ERROR: Emulator not available. Run with --setup-only first.")
            sys.exit(1)

    # ── Run setup steps ──────────────────────────────────────────────
    log(f"Running scenario: {scenario['name']}")
    if scenario["setup_steps"]:
        log("Running setup steps...")
        run_setup_steps(scenario["setup_steps"])
        time.sleep(2)

    # ── Collect initial state ────────────────────────────────────────
    logcat_clear()
    time.sleep(1)
    state = collect_initial_state()

    # ── Build prompt with embedded state ─────────────────────────────
    full_prompt = (
        f"## Scenario: {scenario['name']}\n\n"
        f"{initial_prompt}\n\n"
        f"## Current Emulator State\n{state}\n\n"
        f"Start by taking a screenshot to see the current UI, then proceed with the test plan."
    )

    # ── Create MCP server with our tools ─────────────────────────────
    debugger_server = create_debugger_server()

    # ── Configure agent options ──────────────────────────────────────
    options = ClaudeAgentOptions(
        system_prompt=SYSTEM_PROMPT,
        max_turns=MAX_TURNS,
        mcp_servers={"debugger": debugger_server},
        allowed_tools=[
            "mcp__debugger__run_adb",
            "mcp__debugger__edit_source",
            "mcp__debugger__rebuild_app",
            "mcp__debugger__take_screenshot",
            "mcp__debugger__get_logs",
            "mcp__debugger__mark_resolved",
        ],
        permission_mode="bypassPermissions",
        cwd=GRADLE_ROOT,
        hooks={
            "PreToolUse": [
                HookMatcher(hooks=[log_tool_calls]),
            ],
            "PostToolUse": [
                HookMatcher(
                    matcher="mcp__debugger__mark_resolved",
                    hooks=[check_resolution],
                ),
            ],
        },
    )

    # ── Run the agent ────────────────────────────────────────────────
    log("Handing off to Claude Agent SDK...\n")

    async for message in query(prompt=full_prompt, options=options):
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if hasattr(block, "text"):
                    log(f"Claude: {block.text[:500]}")

        elif isinstance(message, ResultMessage):
            log(f"\n── Agent finished ──")
            log(f"  Status:  {message.subtype}")
            log(f"  Turns:   {message.num_turns}")
            log(f"  Cost:    ${message.total_cost_usd:.4f}" if message.total_cost_usd else "  Cost:    N/A")
            log(f"  Duration: {message.duration_ms / 1000:.1f}s")
            if message.result:
                log(f"  Result:  {message.result[:300]}")

    log("Done.")


# ── Emulator Setup ───────────────────────────────────────────────────

def setup_only():
    """Boot emulator, build app, install, grant permissions."""
    log("Creating AVD...")
    result = create_avd()
    log(result)

    log("Starting emulator...")
    start_emulator(headless=False)

    log("Waiting for boot...")
    if not wait_for_boot(timeout=180):
        log("ERROR: Boot timed out.")
        sys.exit(1)

    log("Emulator booted. Building and installing app...")
    build = subprocess.run(
        ["./gradlew", "installDebug"],
        cwd=GRADLE_ROOT,
        capture_output=True,
        text=True,
        timeout=300,
    )
    if build.returncode != 0:
        log(f"Build failed:\n{build.stderr[-1000:]}")
        sys.exit(1)
    log("App installed.")

    log("Granting permissions...")
    results = setup_permissions()
    for perm, out in results.items():
        log(f"  {perm}: {out or 'ok'}")

    log("Setup complete. Emulator is running.")


# ── CLI ──────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="BePresent Agentic Debugger")
    parser.add_argument("--scenario", type=str, help="Scenario to run")
    parser.add_argument("--prompt", type=str, default="", help="Custom prompt (for freeform)")
    parser.add_argument("--list-scenarios", action="store_true", help="List available scenarios")
    parser.add_argument("--setup-only", action="store_true", help="Only set up emulator + install")
    args = parser.parse_args()

    if args.list_scenarios:
        print("\nAvailable scenarios:\n")
        for key, s in SCENARIOS.items():
            print(f"  {key:25s} {s['description']}")
        print()
        sys.exit(0)

    if args.setup_only:
        setup_only()
        sys.exit(0)

    if not args.scenario:
        parser.print_help()
        sys.exit(1)

    asyncio.run(run_loop(args.scenario, args.prompt))


if __name__ == "__main__":
    main()
