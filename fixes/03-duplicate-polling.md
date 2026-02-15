# Fix 03: Guard Against Duplicate Polling Coroutines

**Resolves:** TD-031

## Problem

Every call to `onStartCommand()` launches a new polling coroutine via `startPolling()` without checking if one is already running:

```kotlin
// MonitoringService.kt:42-46
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startForeground(NOTIFICATION_ID, createMonitoringNotification())
    startPolling()
    return START_STICKY
}
```

```kotlin
// MonitoringService.kt:53-77
private fun startPolling() {
    serviceScope.launch {
        while (isActive) {
            // ... polling loop ...
            delay(1000)
        }
    }
}
```

The service is started via `MonitoringService.start(context)` from:
- `SessionManager.createAndStart()` (line 64)
- `IntentionManager.ensureMonitoringServiceRunning()` (line 138)

Since `startForegroundService()` delivers a new `onStartCommand()` to an already-running service, **each call spawns an additional concurrent polling loop**. With N concurrent loops, the service fires N foreground checks per second, causing duplicate shield launches and unnecessary battery drain.

## Root Cause

No guard exists to prevent re-entry into `startPolling()`. The `serviceScope` is shared across all launches, so multiple coroutines run in parallel within it.

## Fix

Track the polling job and skip launch if already active.

### Before

```kotlin
class MonitoringService : Service() {
    // ...
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastBlockedPackage: String? = null
    private var lastBlockedTime: Long = 0

    // ...

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMonitoringNotification())
        startPolling()
        return START_STICKY
    }

    // ...

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                // ... polling logic ...
                delay(1000)
            }
        }
    }
```

### After

```kotlin
class MonitoringService : Service() {
    // ...
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null                    // ADD
    private var lastBlockedPackage: String? = null
    private var lastBlockedTime: Long = 0

    // ...

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMonitoringNotification())
        startPolling()
        return START_STICKY
    }

    // ...

    private fun startPolling() {
        if (pollingJob?.isActive == true) return            // ADD: guard
        pollingJob = serviceScope.launch {                  // CHANGE: assign to pollingJob
            while (isActive) {
                // ... polling logic unchanged ...
                delay(1000)
            }
        }
    }
```

### Additional import

```kotlin
import kotlinx.coroutines.Job
```

## Why This Approach

- **Minimal change:** A single boolean guard + job reference. No restructuring.
- **Idiomatic:** Standard Kotlin coroutine pattern for ensuring at-most-one active job.
- **Safe on `onDestroy()`:** `serviceScope.cancel()` already cancels all children including `pollingJob`, so cleanup is unaffected.
- **Alternative considered:** Cancelling the old job and starting a new one on each `onStartCommand`. Rejected because there's no reason to restart polling — the loop's behavior doesn't change between starts.

## Files Touched

| File | Change |
|------|--------|
| `MonitoringService.kt` | Add `pollingJob` field, guard in `startPolling()`, import `Job` |

## Verification

1. Start a session (triggers `MonitoringService.start()`).
2. Create an intention (triggers `MonitoringService.start()` again).
3. Open a blocked app -> verify only **one** shield launches (not two stacked).
4. Add logging to `startPolling()`: log on entry and on guard skip. Confirm the second `onStartCommand` hits the guard.
5. Check `adb logcat` for duplicate "polling started" messages — should see exactly one.
