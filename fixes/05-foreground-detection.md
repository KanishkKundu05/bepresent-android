# Fix 05: Foreground App Detection Uses Deprecated Event Type

## Status: FIXED

## Problem
`UsageStatsRepository.detectForegroundApp()` originally only checked `MOVE_TO_FOREGROUND` (event type 1), which was **deprecated in API 29 (Android 10)**. The replacement is `ACTIVITY_RESUMED` (event type 15). On many Android 10+ devices, `MOVE_TO_FOREGROUND` is no longer emitted, causing the function to always return `null` — meaning **no app is ever detected as foreground, and nothing is ever blocked**.

Additionally, the lookback window was only 5 seconds. If an app had been foreground for longer, the transition event would fall out of the window.

**This was the most likely primary cause of "nothing works at all."**

## File
`data/usage/UsageStatsRepository.kt`

## Fix Applied
1. Check both `MOVE_TO_FOREGROUND` and `ACTIVITY_RESUMED` event types
2. Widened lookback window to 10 seconds
3. Added `getCurrentForegroundPackage()` fallback that uses `queryUsageStats()` (not events) when event-based detection returns null
4. `MonitoringService` now maintains `lastKnownForegroundPackage` state so detection persists across polls even if both event and stats queries fail transiently

## Code Changes
- `UsageStatsRepository.kt`: Dual event type check + usage stats fallback
- `MonitoringService.kt`: Persistent `lastKnownForegroundPackage` field
