# Fix 02: Re-block Receiver Must Relaunch Shield When App Still Foreground

**Resolves:** TD-035

## Problem

When an intention's open-window timer expires, `IntentionAlarmReceiver` marks the intention as closed and shows a notification — but does **not** check whether the blocked app is still in the foreground. If the user is still inside the app, they remain unblocked with no shield overlay:

```kotlin
// IntentionAlarmReceiver.kt:41-50
ACTION_REBLOCK -> {
    if (intention != null) {
        dao.setOpenState(intentionId, false, null)
        showNotification(
            context,
            "${intention.appName} time is up",
            "App has been re-shielded",
            intentionId.hashCode() + 100
        )
    }
}
```

The spec explicitly requires: *"If the target app is still in the foreground when the re-block fires, immediately launch the shield activity."*

## Root Cause

The re-block handler was implemented as a simple DB-state flip + notification, without any foreground-detection or shield-launch logic.

## Fix

After flipping the open state, detect the current foreground app and launch `BlockedAppActivity` if it matches the re-blocked package.

### Before

```kotlin
ACTION_REBLOCK -> {
    if (intention != null) {
        dao.setOpenState(intentionId, false, null)
        showNotification(
            context,
            "${intention.appName} time is up",
            "App has been re-shielded",
            intentionId.hashCode() + 100
        )
    }
}
```

### After

```kotlin
ACTION_REBLOCK -> {
    if (intention != null) {
        dao.setOpenState(intentionId, false, null)
        showNotification(
            context,
            "${intention.appName} time is up",
            "App has been re-shielded",
            intentionId.hashCode() + 100
        )

        // If the blocked app is still in the foreground, launch shield immediately
        val usageStatsRepository = UsageStatsRepository(context.applicationContext)
        val foregroundPackage = usageStatsRepository.detectForegroundApp()
        if (foregroundPackage == intention.packageName) {
            val shieldIntent = Intent(context, BlockedAppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, intention.packageName)
                putExtra(BlockedAppActivity.EXTRA_SHIELD_TYPE, BlockedAppActivity.SHIELD_INTENTION)
            }
            context.startActivity(shieldIntent)
        }
    }
}
```

### New imports in IntentionAlarmReceiver.kt

```kotlin
import com.bepresent.android.data.usage.UsageStatsRepository
import com.bepresent.android.features.blocking.BlockedAppActivity
```

### Why `UsageStatsRepository` can be instantiated directly

`UsageStatsRepository` is a simple wrapper around `UsageStatsManager` with no dependencies beyond `Context` (see `UsageStatsRepository.kt:17-21`). Since the `BroadcastReceiver` already uses a manual Room instance (not Hilt), constructing `UsageStatsRepository` directly is consistent with the current pattern. This can be migrated to DI when TD-036 is addressed.

### `detectForegroundApp()` reference

```kotlin
// UsageStatsRepository.kt:42-55
fun detectForegroundApp(): String? {
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 5000
    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
    var lastForegroundPackage: String? = null
    val event = UsageEvents.Event()
    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            lastForegroundPackage = event.packageName
        }
    }
    return lastForegroundPackage
}
```

## Files Touched

| File | Change |
|------|--------|
| `IntentionAlarmReceiver.kt` | Add foreground check + shield launch in `ACTION_REBLOCK` handler; add imports |

## Verification

1. Create an intention with a 1-minute open window.
2. Open the app via the shield "Open" button.
3. Stay inside the app past the timer expiry.
4. **Expected:** Shield activity appears immediately on top of the app when the re-block alarm fires.
5. Also verify the notification ("time is up") still appears alongside the shield.
6. Test the case where the user has already left the app before timer expiry -> no shield launch, notification only.
