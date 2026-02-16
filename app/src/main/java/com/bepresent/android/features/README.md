# Features Module

Core business logic for BePresent's screentime blocking functionality.

## Directory Structure

```
features/
├── sessions/          # Focus session management
│   ├── SessionManager.kt
│   └── SessionStateMachine.kt
├── intentions/        # App-specific usage intentions
│   ├── IntentionManager.kt
│   └── DailyResetWorker.kt
└── blocking/          # App interception and shield UI
    ├── BlockedAppActivity.kt
    └── ShieldScreen.kt
```

## Sessions

**SessionManager** orchestrates focus sessions:
- Creates sessions with name, duration, blocked apps, and optional beast mode
- Manages session lifecycle: `idle -> active -> goalReached -> completed`
- Schedules alarm for goal reached notification
- Starts/stops `MonitoringService` based on active sessions
- Awards XP and coins on completion via `PreferencesManager`
- Enqueues completed sessions for Convex sync

**SessionStateMachine** enforces valid state transitions:
- `cancel()` - Only allowed within first 10 seconds
- `giveUp()` - Only allowed if beast mode is OFF
- `goalReached()` - Transitions active session when timer completes
- `complete()` - Finalizes session after goal reached
- `calculateRewards()` - XP/coin rewards based on duration (3-25 points)

## Intentions

**IntentionManager** handles per-app usage limits:
- Tracks allowed opens per day and time per open
- `openApp()` - Increments counter, starts timer, schedules reblock alarm
- `reblockApp()` - Called by alarm to re-enable blocking
- Schedules 30-second warning before reblock
- Maintains streak tracking (consecutive days within limit)

**DailyResetWorker** runs at midnight:
- Resets daily open counters for all intentions
- Updates streaks: +1 if within limit, reset to 0 if exceeded
- Grants weekly streak freeze on Mondays
- Consumes streak freeze if any intention exceeded limit
- Enqueues daily stats sync to Convex

## Blocking

**BlockedAppActivity** is launched by `MonitoringService` when a blocked app is detected.

**ShieldScreen** renders three shield variants:
1. **SessionShield** - During active focus session (shows "Be Present" button)
2. **GoalReachedShield** - Session goal complete (shows XP reward and Complete button)
3. **IntentionShield** - App intention block (shows opens remaining, streak, "Open Anyway" option)

## Data Flow

```
User opens blocked app
        ↓
MonitoringService detects via UsageStatsRepository
        ↓
Launches BlockedAppActivity with shield type
        ↓
ShieldScreen renders appropriate UI
        ↓
User action → SessionManager/IntentionManager
        ↓
State persisted to Room DB
        ↓
SyncManager enqueues for Convex sync
```
