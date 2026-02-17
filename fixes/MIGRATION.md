# Screentime Enforcement — Migration & Architecture Reference

This document replaces the individual fix files (01–11). It describes the problems found in the original blocking pipeline, how they were resolved, and the core patterns now in use.

---

## 1. What Broke

The app's screentime enforcement pipeline was non-functional on physical devices. Setting an intention on an app did not block it, opens were not registered, and the shield overlay never appeared. A full audit identified 10 bugs across the pipeline plus structural issues in the manager layer.

### Critical path failures

| Area | Problem | Root file(s) |
|------|---------|--------------|
| **Foreground detection** | `detectForegroundApp()` used the deprecated `MOVE_TO_FOREGROUND` event type, which Android 10+ no longer emits. The monitoring service could never identify which app was in the foreground. | `UsageStatsRepository.kt` |
| **Overlay permission** | `SYSTEM_ALERT_WINDOW` was missing from the manifest. Android silently suppressed `BlockedAppActivity` launches from the background service. | `AndroidManifest.xml`, `PermissionManager.kt` |
| **Onboarding gap** | The onboarding flow never prompted the user to grant overlay permission, so even after the manifest fix the shield stayed suppressed. | `OnboardingScreen.kt` |

### Compounding failures

| Area | Problem | Root file(s) |
|------|---------|--------------|
| **Service lifecycle** | `MonitoringService.checkAndStop()` unconditionally stopped the service. Ending a session killed intention monitoring; deleting the last intention killed session monitoring. | `MonitoringService.kt` |
| **Re-block shield** | When an intention's open-window timer expired, `IntentionAlarmReceiver` flipped the DB flag but did not check if the user was still inside the blocked app. No shield appeared. | `IntentionAlarmReceiver.kt` |
| **Duplicate polling** | Each `onStartCommand()` spawned a new polling coroutine without checking if one was already running, causing duplicate shield launches and battery drain. | `MonitoringService.kt` |
| **Streak freeze** | A per-iteration boolean gate meant the freeze only protected the first over-limit intention. A Monday grant/consume ordering bug made freezes unconsumed on Mondays. | `DailyResetWorker.kt` |
| **Silent exceptions** | A bare `catch (_: Exception)` in the polling loop swallowed all errors, making every other failure invisible. | `MonitoringService.kt` |
| **Back gesture bypass** | The deprecated `onBackPressed()` override was never called on Android 13+ predictive back, letting users swipe past the shield. | `BlockedAppActivity.kt` |
| **Receiver Room crash** | `IntentionAlarmReceiver` and `BootCompletedReceiver` built bare Room instances without migration support, crashing on schema v2. | `IntentionAlarmReceiver.kt`, `BootCompletedReceiver.kt` |

---

## 2. How They Were Fixed

### Detection & permissions layer

**`UsageStatsRepository.detectForegroundApp()`** now checks both `MOVE_TO_FOREGROUND` and `ACTIVITY_RESUMED` event types, widens the lookback window from 5s to 10s, and falls back to `queryUsageStats()` if no events are found. The service caches `lastKnownForegroundPackage` across polls so a single missed tick doesn't drop detection.

**`AndroidManifest.xml`** declares `SYSTEM_ALERT_WINDOW`. **`PermissionManager`** exposes `hasOverlayPermission()` and `getOverlayPermissionIntent()`. **`OnboardingScreen`** has a new `STEP_OVERLAY` step that prompts the user to grant it.

### Service lifecycle

**`MonitoringService.checkAndStop()`** now takes `PresentSessionDao` and `AppIntentionDao` as parameters, queries both, and only stops when there are zero active sessions AND zero intentions. Every call site (`SessionManager`, `IntentionManager`) passes these DAOs.

**Duplicate polling** is prevented by tracking the polling `Job` and skipping `startPolling()` if `pollingJob?.isActive == true`.

**Silent exceptions** are replaced with `Log.e()` so polling errors appear in logcat.

### Shield enforcement

**Re-block shield:** `IntentionAlarmReceiver.ACTION_REBLOCK` now calls `UsageStatsRepository.detectForegroundApp()` after flipping the DB state. If the foreground app matches the re-blocked package, it launches `BlockedAppActivity` immediately.

**Back gesture:** `BlockedAppActivity` registers an `OnBackPressedCallback` on the dispatcher that routes to `navigateHome()`, replacing the deprecated `onBackPressed()` override.

### Data integrity

**Receiver Room crash:** Both receivers add `.fallbackToDestructiveMigration()` to their Room builder calls.

**Streak freeze:** The freeze-consumption loop was refactored to pre-scan all intentions and apply a single `freezeActive` boolean uniformly. Monday grant was moved before consumption so a just-granted freeze can be properly consumed in the same run.

---

## 3. Core Patterns

### Declarative state machine (`SessionStateMachine`)

Session transitions are defined as pure functions that return a `Transition` data class declaring both the new state and all required side effects:

```
SessionStateMachine.kt

object SessionStateMachine {
    data class Transition(
        val newState: String,
        val action: String,              // PresentSessionAction.ACTION_*
        val cancelAlarm: Boolean,
        val clearActiveSession: Boolean,
        val syncAfter: Boolean,
        val rewardsEligible: Boolean,
        val setEndedAt: Boolean,
        val setGoalReachedAt: Boolean
    )

    sealed class TransitionResult {
        data class Success(val transition: Transition) : TransitionResult()
        data class Error(val message: String) : TransitionResult()
    }
}
```

Each transition function (`cancel`, `giveUp`, `goalReached`, `complete`) validates the precondition and returns a `Transition` with the appropriate flags. The state machine has **no dependencies** — it is a pure object that can be unit-tested without mocking anything.

**Valid transitions:**

```
IDLE ──start──> ACTIVE
ACTIVE ──cancel (≤10s)──> CANCELED
ACTIVE ──giveUp (no beast mode)──> GAVE_UP
ACTIVE ──goalReached──> GOAL_REACHED
GOAL_REACHED ──complete──> COMPLETED
```

### Generic transition executor (`SessionManager.applyTransition`)

`SessionManager` has a single private method that reads the declarative `Transition` and executes all side effects:

```
SessionManager.kt

private suspend fun applyTransition(
    sessionId: String,
    transitionFn: (PresentSession) -> SessionStateMachine.TransitionResult
): Boolean
```

It handles: DB persistence, action logging, alarm cancellation, preference cleanup, service lifecycle, reward calculation, and sync. Each public method is a one-liner:

```kotlin
suspend fun cancel(sessionId: String) = applyTransition(sessionId, SessionStateMachine::cancel)
suspend fun giveUp(sessionId: String) = applyTransition(sessionId, SessionStateMachine::giveUp)
suspend fun goalReached(sessionId: String) = applyTransition(sessionId, SessionStateMachine::goalReached)
suspend fun complete(sessionId: String) = applyTransition(sessionId, SessionStateMachine::complete)
```

Adding a new transition means: define it in `SessionStateMachine` with the right flags, then add a one-liner in `SessionManager`. No boilerplate duplication.

### Extracted alarm schedulers

Alarm scheduling is pure infrastructure (Android `AlarmManager` + `PendingIntent` wiring) extracted into dedicated Hilt singletons:

| Class | Responsibility |
|-------|---------------|
| `SessionAlarmScheduler` | Schedule/cancel the goal-reached alarm for focus sessions |
| `IntentionAlarmScheduler` | Schedule/cancel the warning (T-30s) and re-block alarms for intention timers |

Both are `@Singleton` with `@Inject constructor(@ApplicationContext context: Context)`. The managers delegate to them rather than owning alarm logic directly.

### Two blocking systems, one monitoring service

The app has two independent blocking systems that feed into a single enforcement layer:

**Sessions** — temporary focus blocks. User picks apps to block for a timed session. Blocked packages are stored as a JSON array on the `PresentSession` entity. Lifecycle is managed by the state machine.

**Intentions** — persistent per-app rules. User sets an always-on rule (e.g., "Instagram: 3 opens/day, 5 min each"). State is managed via `AppIntentionDao` — open/closed toggle, open count, streaks. `IntentionAlarmScheduler` handles timed re-blocking.

**`MonitoringService`** merges both: it unions the session's blocked packages with intention-blocked packages (`currentlyOpen = false`) and polls the foreground app every second. Shield type is determined by which system flagged the package (sessions take priority).

**`MonitoringService.checkAndStop()`** is the single teardown gate: it queries both `PresentSessionDao` and `AppIntentionDao` and only stops the service when both are empty.

### Receiver pattern (non-Hilt)

`IntentionAlarmReceiver` and `BootCompletedReceiver` are `BroadcastReceiver` subclasses that cannot use Hilt constructor injection. They build a manual Room instance with `.fallbackToDestructiveMigration()`, do their work inside `goAsync()` + `CoroutineScope(Dispatchers.IO)`, and close the DB when done. `UsageStatsRepository` is constructed directly (it only needs `Context`).

---

## 4. File Map

### Sessions (`features/sessions/`)

| File | Role |
|------|------|
| `SessionStateMachine.kt` | Pure state machine — validates transitions, declares side effects, calculates rewards |
| `SessionManager.kt` | Orchestrator — `createAndStart`, `applyTransition`, delegates to alarm scheduler |
| `SessionAlarmScheduler.kt` | Alarm infrastructure — schedule/cancel goal-reached alarms via `AlarmManager` |

### Intentions (`features/intentions/`)

| File | Role |
|------|------|
| `IntentionManager.kt` | CRUD + open/reblock lifecycle, delegates alarm work to scheduler |
| `IntentionAlarmScheduler.kt` | Alarm infrastructure — schedule/cancel warning + re-block alarms |

### Enforcement (`service/`)

| File | Role |
|------|------|
| `MonitoringService.kt` | Foreground polling loop, blocked-package resolution, shield launch, service lifecycle |
| `IntentionAlarmReceiver.kt` | Handles timer expiry — flips DB state, launches shield if app still foreground |
| `SessionAlarmReceiver.kt` | Handles goal-reached alarm — triggers `SessionManager.goalReached()` |
| `BootCompletedReceiver.kt` | Restarts `MonitoringService` on device boot if sessions/intentions exist |

### Data (`data/`)

| File | Role |
|------|------|
| `AppIntentionDao.kt` | Room DAO for intentions — CRUD, open-state toggle, open-count increment, reactive `Flow` queries |
| `PresentSessionDao.kt` | Room DAO for sessions — CRUD, active session observation, action logging |
| `UsageStatsRepository.kt` | Foreground detection via `UsageStatsManager` — dual event type check + `queryUsageStats` fallback |

### Permissions & onboarding

| File | Role |
|------|------|
| `PermissionManager.kt` | Checks/requests usage stats, overlay, and notification permissions |
| `OnboardingScreen.kt` | Step-by-step permission grant flow including overlay permission step |
| `BlockedAppActivity.kt` | Shield overlay — `OnBackPressedCallback` prevents bypass on Android 13+ |
