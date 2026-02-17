# App Blocking Pipeline — Full Audit Summary

## Context
User tested on a physical phone: set an intention on Clock app, but the app was NOT blocked when opened and opens were NOT being registered. The entire blocking pipeline was non-functional.

## Root Cause
**Issue 05 (foreground detection)** was the primary cause. `detectForegroundApp()` used the deprecated `MOVE_TO_FOREGROUND` event type which doesn't fire on Android 10+. This meant the monitoring service could never detect which app was in the foreground — so nothing was ever blocked, regardless of whether the shield launching or permission systems worked.

The remaining 5 issues compounded the problem: even if detection worked, the shield would be silently suppressed (no overlay permission), back gestures could bypass it, receivers would crash on schema v2, and errors were invisible.

## Issues Found

| # | File | Severity | Summary |
|---|------|----------|---------|
| [05](05-foreground-detection.md) | UsageStatsRepository.kt, MonitoringService.kt | **Critical** | Deprecated event type = no detection on Android 10+ |
| [06](06-overlay-permission.md) | AndroidManifest.xml, PermissionManager.kt | **Critical** | No SYSTEM_ALERT_WINDOW = shield silently suppressed |
| [07](07-silent-exception.md) | MonitoringService.kt | **High** | Silent catch hides all errors |
| [08](08-back-gesture.md) | BlockedAppActivity.kt | **Medium** | Deprecated onBackPressed bypassed on Android 13+ |
| [09](09-receiver-room-migration.md) | IntentionAlarmReceiver.kt, BootCompletedReceiver.kt | **High** | Missing migration crashes receivers on schema v2 |
| [10](10-onboarding-overlay.md) | OnboardingScreen.kt | **High** | No runtime overlay permission step |

## Previous Fixes (01-04)
- [01](01-service-lifecycle.md) — Service lifecycle issues
- [02](02-reblock-shield.md) — Re-block shield not appearing
- [03](03-duplicate-polling.md) — Duplicate polling loops
- [04](04-streak-freeze.md) — Streak freeze bug

## Files Modified

| File | Issues |
|------|--------|
| `data/usage/UsageStatsRepository.kt` | 05 |
| `service/MonitoringService.kt` | 05, 07 |
| `AndroidManifest.xml` | 06 |
| `permissions/PermissionManager.kt` | 06, 10 |
| `features/blocking/BlockedAppActivity.kt` | 08 |
| `service/IntentionAlarmReceiver.kt` | 09 |
| `service/BootCompletedReceiver.kt` | 09 |
| `ui/onboarding/OnboardingScreen.kt` | 10 |

## Verification Checklist
1. Grant usage stats + overlay + notification permissions via onboarding
2. Create an intention on Clock app (3 opens/day, 5 min each)
3. Open Clock from home screen → shield should appear within ~1 second
4. Tap "Open Clock" → app opens, `totalOpensToday` increments to 1
5. Wait for timer to expire → shield reappears
6. Check dashboard shows 1/3 opens
7. Reboot device → monitoring service restarts, blocking still works

## All Issues: FIXED
