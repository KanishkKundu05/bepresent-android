# Fix 01: Service Lifecycle — `checkAndStop()` Must Check State Before Stopping

**Resolves:** TD-020, TD-029, TD-032, TD-050

## Problem

`MonitoringService.checkAndStop()` is a placeholder that unconditionally stops the service:

```kotlin
// MonitoringService.kt:186-191
fun checkAndStop(context: Context) {
    // Service will self-check if it needs to keep running
    // For now, we just stop if explicitly called
    // The service can be restarted if needed
    stop(context)
}
```

This method is called from three places — all session-end transitions:

- `SessionManager.cancel()` (line 84)
- `SessionManager.giveUp()` (line 105)
- `SessionManager.complete()` (line 153)

And from the intention-delete path:

- `IntentionManager.delete()` (line 61)

**Result:** Ending a session kills monitoring even if intentions are still active. Deleting the last intention kills monitoring even if a session is active. Intention-based blocking silently dies mid-session.

## Root Cause

`checkAndStop()` was stubbed as `stop()` during initial implementation and never revisited. The actual lifecycle rule is: **stop only when there are zero active sessions AND zero intentions**.

## Fix

Replace the unconditional `stop()` with an async check against both `PresentSessionDao` and `AppIntentionDao`:

### Before

```kotlin
// MonitoringService.kt companion object
fun checkAndStop(context: Context) {
    stop(context)
}
```

### After

```kotlin
// MonitoringService.kt companion object
fun checkAndStop(context: Context, sessionDao: PresentSessionDao, intentionDao: AppIntentionDao) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        val hasActiveSession = sessionDao.getActiveSession() != null
        val hasIntentions = intentionDao.getCount() > 0
        if (!hasActiveSession && !hasIntentions) {
            stop(context)
        }
    }
}
```

### Call-site updates

Every call site already has access to the required DAOs (or can obtain them). Update each:

**SessionManager.kt** (lines 84, 105, 153) — inject `intentionDao` into SessionManager:

```kotlin
// SessionManager constructor — add parameter
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDao: PresentSessionDao,
    private val intentionDao: AppIntentionDao,   // ADD
    private val preferencesManager: PreferencesManager
) {
```

```kotlin
// Replace each call site (cancel, giveUp, complete):
// Before:
MonitoringService.checkAndStop(context)

// After:
MonitoringService.checkAndStop(context, sessionDao, intentionDao)
```

**IntentionManager.kt** (line 61) — already has `intentionDao`, inject `sessionDao`:

```kotlin
@Singleton
class IntentionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentionDao: AppIntentionDao,
    private val sessionDao: PresentSessionDao    // ADD
) {
```

```kotlin
// Before (IntentionManager.kt:57-62):
if (intentionDao.getCount() == 0) {
    MonitoringService.checkAndStop(context)
}

// After:
MonitoringService.checkAndStop(context, sessionDao, intentionDao)
// (remove the count==0 guard — checkAndStop now handles the logic itself)
```

## Files Touched

| File | Change |
|------|--------|
| `MonitoringService.kt` | Replace `checkAndStop()` body with async DB check |
| `SessionManager.kt` | Add `intentionDao` constructor param; update 3 call sites |
| `IntentionManager.kt` | Add `sessionDao` constructor param; simplify delete path |

## Verification

1. Create an intention, start a session, end the session -> service stays running (intention still exists).
2. Delete all intentions while session is active -> service stays running.
3. End session when no intentions exist -> service stops.
4. Delete last intention when no session is active -> service stops.
5. `adb shell dumpsys activity services | grep MonitoringService` confirms expected start/stop state after each scenario.
