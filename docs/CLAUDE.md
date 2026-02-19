# BePresent Android — Claude Code Context

## Project Overview
BePresent is a digital wellbeing app for Android. MVP is a single-screen dashboard with two core features: **App Intentions** (per-app daily open limits) and **Blocking Sessions** (timed focus sessions). Local-first with optional Convex/Auth0 integration.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **Database**: Room (`bepresent.db`)
- **Preferences**: DataStore (`bepresent_prefs`)
- **Background**: Foreground Service (monitoring), WorkManager (daily reset), AlarmManager (timers)
- **Min SDK**: 26 (Android 8) / **Target SDK**: 34 (Android 14)
- **Build**: AGP 8.2.2, Kotlin 1.9.22, KSP, Gradle 8.5

## Architecture
Single-module app. Single Activity (`MainActivity`) hosting Compose. `BlockedAppActivity` runs in a separate task for shield screens.

### Key Directories
```
app/src/main/java/com/bepresent/android/
├── data/db/          # Room entities + DAOs (AppIntention, PresentSession)
├── data/datastore/   # PreferencesManager (XP, coins, streaks, flags)
├── data/usage/       # UsageStatsRepository (screen time, foreground detection)
├── di/               # Hilt AppModule
├── features/
│   ├── blocking/     # BlockedAppActivity + ShieldScreen (3 variants)
│   ├── intentions/   # IntentionManager + DailyResetWorker
│   └── sessions/     # SessionManager + SessionStateMachine
├── service/          # MonitoringService, BootCompletedReceiver, alarm receivers
├── permissions/      # PermissionManager + OemBatteryGuide
└── ui/               # All Compose UI (dashboard, components, sheets, onboarding, theme)
```

### Data Flow
- `DashboardViewModel` combines Room flows + DataStore flows + polled screen time
- `MonitoringService` polls UsageStats every 1s, launches `BlockedAppActivity` when blocked app detected
- `IntentionManager` handles timed open windows via AlarmManager
- `SessionManager` drives state machine transitions and XP awards
- `DailyResetWorker` runs at midnight for streak updates and freeze grants

## Conventions
- Commit messages: lowercase, concise, imperative
- UsageStats polling is the active foreground-detection path
- Accessibility service is permission-gated and reserved for future event-driven detection
- Soft enforcement: users can always "Open Anyway" (streak breaks)
- Session priority over intentions when both block the same app

## Spec Documents
Planning/spec source files were removed from this repository.
Use current implementation docs instead:
- `README.md`
- `docs/features.md`
- `docs/permissions-and-enforcement.md`
