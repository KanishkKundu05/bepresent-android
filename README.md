# BePresent Android

Android app for enforcing focused, distraction-free sessions.

## Repo status (rechecked February 19, 2026)
- `tech-debt` and planning artifacts have been removed from this repository.
- Local database is active (Room + DAOs + DI wiring are live in app runtime).
- Blocking enforcement is active (foreground monitoring + shield activity + session/intention state).

## What the app does
- App Intentions: per-app daily open limits with timed unlock windows.
- Blocking Sessions: timed focus sessions that block selected apps.
- Shield Interruption: `BlockedAppActivity` is launched when a blocked app is detected.
- Rewards: XP/coins for completed sessions.

## Permission and enforcement docs
- `docs/features.md`
- `docs/permissions-and-enforcement.md`

These docs include exactly which permissions are requested from users, and how each permission is used in blocked-session enforcement.

## Quick start
1. Open project in Android Studio (JDK 17, Android SDK 34).
2. Sync Gradle.
3. Run `app` on API 26+ device/emulator.
4. Complete onboarding permission flow.

## Core code map
- `app/src/main/java/com/bepresent/android/service/MonitoringService.kt`
- `app/src/main/java/com/bepresent/android/features/blocking/BlockedAppActivity.kt`
- `app/src/main/java/com/bepresent/android/features/sessions/SessionManager.kt`
- `app/src/main/java/com/bepresent/android/features/intentions/IntentionManager.kt`
- `app/src/main/java/com/bepresent/android/data/db/BePresentDatabase.kt`
- `app/src/main/java/com/bepresent/android/permissions/PermissionManager.kt`
