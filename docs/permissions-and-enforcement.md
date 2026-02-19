# Permissions and Blocked-Session Enforcement

This is the source-of-truth mapping between Android permissions and current enforcement behavior.

## Enforcement pipeline
1. User grants required permissions in onboarding/settings.
2. `MonitoringService` polls foreground app via `UsageStatsRepository`.
3. Service builds blocked package set from active session + active intentions.
4. If foreground app is blocked, service launches `BlockedAppActivity`.
5. Shield actions update Room state via `SessionManager` / `IntentionManager`.
6. Timers/alarms re-block apps and transition sessions.

Primary files:
- `app/src/main/java/com/bepresent/android/service/MonitoringService.kt`
- `app/src/main/java/com/bepresent/android/data/usage/UsageStatsRepository.kt`
- `app/src/main/java/com/bepresent/android/features/blocking/BlockedAppActivity.kt`

## Permissions requested from users

### 1) Usage Access (`android.permission.PACKAGE_USAGE_STATS`)
- Request path:
  - Onboarding opens `Settings.ACTION_USAGE_ACCESS_SETTINGS`.
  - `PermissionManager.hasUsageStatsPermission()` validates status.
- Enforcement use:
  - Required for foreground app detection (`detectForegroundApp()`).
  - Without it, blocked-app interception cannot work reliably.

Code references:
- `app/src/main/java/com/bepresent/android/permissions/PermissionManager.kt`
- `app/src/main/java/com/bepresent/android/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/bepresent/android/data/usage/UsageStatsRepository.kt`

### 2) Overlay (`android.permission.SYSTEM_ALERT_WINDOW`)
- Request path:
  - Onboarding opens `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
  - `PermissionManager.hasOverlayPermission()` validates status.
- Enforcement use:
  - Included in critical permission gate (`criticalGranted`).
  - Used as part of readiness checks before considering app monitoring fully enabled.

Code references:
- `app/src/main/java/com/bepresent/android/permissions/PermissionManager.kt`
- `app/src/main/java/com/bepresent/android/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/bepresent/android/ui/dashboard/DashboardViewModel.kt`

### 3) Accessibility service enablement
- Request path:
  - Onboarding opens `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
  - `PermissionManager.hasAccessibilityPermission()` checks whether `AccessibilityMonitorService` is enabled.
- Enforcement use:
  - Included in critical permission gate (`criticalGranted`).
  - Service class is present; current `onAccessibilityEvent` is reserved/no-op.

Code references:
- `app/src/main/java/com/bepresent/android/permissions/PermissionManager.kt`
- `app/src/main/java/com/bepresent/android/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/bepresent/android/service/AccessibilityMonitorService.kt`

## Other permissions used by enforcement runtime
These are not the primary onboarding critical-gate permissions, but they support reliable blocking.

| Permission | Runtime use |
|---|---|
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps `MonitoringService` alive while blocking is active. |
| `USE_EXACT_ALARM`, `WAKE_LOCK` | Precise timers for re-block windows and session-goal events. |
| `RECEIVE_BOOT_COMPLETED` | Restarts monitoring after reboot if sessions/intentions exist. |
| `POST_NOTIFICATIONS` | Alerts for session goal reached, re-block warnings, and monitoring state. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional OEM reliability path for long-running monitoring. |
| `QUERY_ALL_PACKAGES` | Supports complete app visibility in picker flows. |

Manifest source:
- `app/src/main/AndroidManifest.xml`

## Database status recheck
Rechecked February 19, 2026: local database is active.

Evidence in code:
- Room dependencies configured in `app/build.gradle.kts`.
- `BePresentDatabase` is provided by Hilt in `AppModule`.
- Session/intention flows persist and read data via DAOs during runtime and alarms.

Key files:
- `app/src/main/java/com/bepresent/android/data/db/BePresentDatabase.kt`
- `app/src/main/java/com/bepresent/android/di/AppModule.kt`
- `app/src/main/java/com/bepresent/android/features/sessions/SessionManager.kt`
- `app/src/main/java/com/bepresent/android/features/intentions/IntentionManager.kt`
- `app/src/main/java/com/bepresent/android/service/IntentionAlarmReceiver.kt`
- `app/src/main/java/com/bepresent/android/service/SessionAlarmReceiver.kt`
