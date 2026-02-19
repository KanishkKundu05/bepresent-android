# Features

This document describes shipped behavior in the current Android codebase.

## 1. App Intentions
Per-app daily open limits with temporary unlock windows.

### User flow
1. User selects an app from the app picker.
2. User sets opens/day and minutes/open.
3. App stays blocked unless it is inside an active unlock window.
4. When the window expires, the app is re-blocked.

### Enforcement behavior
- `MonitoringService` checks foreground app every ~1 second.
- If a blocked app is detected, `BlockedAppActivity` is launched.
- User can open temporarily from the shield (intention flow), then app is re-blocked by alarm.

Key files:
- `app/src/main/java/com/bepresent/android/features/intentions/IntentionManager.kt`
- `app/src/main/java/com/bepresent/android/service/MonitoringService.kt`
- `app/src/main/java/com/bepresent/android/service/IntentionAlarmReceiver.kt`

## 2. Blocking Sessions
Timed focus sessions that block selected apps.

### User flow
1. User picks blocked apps and session duration.
2. Session starts and monitoring runs continuously.
3. Opening blocked apps triggers session shield.
4. Goal reached moves session to `goalReached` state; completion awards XP/coins.

### Session states
`idle -> active -> goalReached -> completed`

Alternate exits:
- `gaveUp`
- `canceled` (early cancel path)

Key files:
- `app/src/main/java/com/bepresent/android/features/sessions/SessionManager.kt`
- `app/src/main/java/com/bepresent/android/features/sessions/SessionStateMachine.kt`
- `app/src/main/java/com/bepresent/android/service/SessionAlarmReceiver.kt`

## 3. Shield interruption UX
`BlockedAppActivity` renders shield variants for:
- Session block
- Intention block
- Goal reached

Key files:
- `app/src/main/java/com/bepresent/android/features/blocking/BlockedAppActivity.kt`
- `app/src/main/java/com/bepresent/android/features/blocking/ShieldScreen.kt`

## 4. Permissions requested from user
These are the permissions actively requested in onboarding/system settings flow and used for blocking capabilities.

| Permission | Requested from user | How it is used |
|---|---|---|
| Usage Access (`PACKAGE_USAGE_STATS`) | Yes | Detect foreground app and screen-time usage for block enforcement. |
| Overlay (`SYSTEM_ALERT_WINDOW`) | Yes | Treated as critical permission gate; used as part of shield-launch capability checks. |
| Accessibility Service | Yes | User is prompted to enable service; currently used as a required gate for enforcement readiness checks. |

Important implementation note:
- Foreground detection currently runs through `UsageStatsRepository`.
- `AccessibilityMonitorService` exists but event handling is currently reserved (no active event processing).

## 5. Other declared permissions (not core enforcement gate)
| Permission | Current role |
|---|---|
| `POST_NOTIFICATIONS` | Session/intention notifications. |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keep monitoring service running. |
| `RECEIVE_BOOT_COMPLETED` | Restart monitoring after reboot when needed. |
| `USE_EXACT_ALARM`, `WAKE_LOCK` | Reliable timed re-block and session-goal alarms. |
| `QUERY_ALL_PACKAGES` | App picker visibility for installed apps. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional reliability improvement on OEM devices. |
| `INTERNET` | Convex/auth network operations. |

## 6. Database status (rechecked)
Database is active and used at runtime:
- Room database: `BePresentDatabase`
- Entities: sessions, session actions, app intentions, sync queue
- DAOs are injected through Hilt and used by managers/services

Key files:
- `app/src/main/java/com/bepresent/android/data/db/BePresentDatabase.kt`
- `app/src/main/java/com/bepresent/android/di/AppModule.kt`
- `app/src/main/java/com/bepresent/android/features/sessions/SessionManager.kt`
- `app/src/main/java/com/bepresent/android/features/intentions/IntentionManager.kt`
