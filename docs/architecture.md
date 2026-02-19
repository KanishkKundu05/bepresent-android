# Architecture

## Overview

BePresent Android is a single-module Jetpack Compose app built with a layered architecture:

```
┌─────────────────────────────────────────────┐
│                    UI Layer                  │
│  Compose Screens → ViewModels → StateFlow   │
├─────────────────────────────────────────────┤
│                 Feature Layer                │
│  IntentionManager, SessionManager,          │
│  SessionStateMachine                        │
├─────────────────────────────────────────────┤
│                 Service Layer                │
│  MonitoringService, AlarmReceivers,         │
│  BootCompletedReceiver                      │
├─────────────────────────────────────────────┤
│                  Data Layer                  │
│  Room DB, DataStore, UsageStatsRepository   │
└─────────────────────────────────────────────┘
```

## Key Design Decisions

### 1. Full-Screen Activity for Shield
`BlockedAppActivity` runs in its own task (`taskAffinity=""`, `singleTask`) and is launched by `MonitoringService` when a blocked app is detected.

### 2. UsageStats Polling with Accessibility Gate
`MonitoringService` polls `UsageStatsManager.queryEvents()` every 1 second for foreground detection. Accessibility service enablement is currently required by permission gating, but accessibility event processing is reserved for future integration.

### 3. Room + DataStore Split
- **Room**: Structured entities with relationships (intentions, sessions, actions)
- **DataStore**: Simple key-value preferences (XP, coins, flags, onboarding state)

### 4. Foreground Service for Monitoring
`MonitoringService` uses `START_STICKY` and `FOREGROUND_SERVICE_SPECIAL_USE` type. It runs whenever intentions exist or a session is active, and stops when neither applies.

### 5. AlarmManager for Timed Events
`setAlarmClock()` is used for intention re-block timers and session goal alarms. This survives Doze mode and is the most reliable timing mechanism on Android.

## State Management

### Dashboard
`DashboardViewModel` combines 6 reactive sources into a single `DashboardUiState`:
- Screen time (polled every 30s via `MutableStateFlow`)
- Per-app usage (polled every 30s)
- Intentions (Room `Flow`)
- Active session (Room `Flow`)
- Total XP (DataStore `Flow`)
- Total coins (DataStore `Flow`)

### Session State Machine
```
idle → active → goalReached → completed
         ↓          ↓
       gaveUp    (continues blocking, user can end later)
         ↓
       canceled (only within first 10 seconds)
```

### Blocked List
The union of:
1. Session blocked packages (from active session's JSON array)
2. Intention blocked packages (where `currentlyOpen == false`)

Session shield takes priority when both apply to the same app.

## Dependency Injection
Hilt provides all dependencies. Key bindings:
- `AppModule`: Room database + DAOs (singleton)
- `PreferencesManager`: DataStore wrapper (singleton, constructor-injected)
- `UsageStatsRepository`: UsageStatsManager wrapper (singleton)
- `IntentionManager`, `SessionManager`: Feature orchestrators (singleton)
- `PermissionManager`: Permission checks (singleton)

## Background Scheduling
- **MonitoringService**: Continuous foreground service while blocking is active
- **DailyResetWorker**: WorkManager periodic task (daily, starting at midnight)
- **IntentionAlarmReceiver**: AlarmManager for timed open window expiry
- **SessionAlarmReceiver**: AlarmManager for session goal reached
- **BootCompletedReceiver**: Restarts monitoring service after device reboot
