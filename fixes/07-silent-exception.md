# Fix 07: Silent Exception Swallowing in MonitoringService

## Status: FIXED

## Problem
The polling loop in `MonitoringService.startPolling()` had a catch block that silently swallowed all exceptions:

```kotlin
} catch (_: Exception) {
    // Silently handle usage stats errors
}
```

If usage stats permission wasn't granted, or if `queryEvents()` threw, or ANY error occurred — it was invisible. The polling loop continued but did nothing. The user sees the "Monitoring" notification and thinks it's working.

## File
`service/MonitoringService.kt`

## Fix Applied
Replaced silent catch with `Log.e()`:

```kotlin
} catch (e: Exception) {
    Log.e("MonitoringService", "Polling error", e)
}
```

Errors are now visible in logcat for debugging.
