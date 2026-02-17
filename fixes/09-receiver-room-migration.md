# Fix 09: Raw Room DB in Receivers Crashes on Schema v2

## Status: FIXED

## Problem
Both `IntentionAlarmReceiver` and `BootCompletedReceiver` built bare Room database instances without migration support. The app's database is at schema v2 (added `SyncQueueItem` entity). Without `fallbackToDestructiveMigration()`, Room throws `IllegalStateException` when opening a v2 database with a v1 builder — silently failing via the catch block.

This meant:
- **BootCompletedReceiver**: After device reboot, the monitoring service was never restarted
- **IntentionAlarmReceiver**: Re-block alarms silently failed, so timed opens never expired

## Files
- `service/IntentionAlarmReceiver.kt`
- `service/BootCompletedReceiver.kt`

## Fix Applied
Added `.fallbackToDestructiveMigration()` to both Room builder calls:

```kotlin
val db = Room.databaseBuilder(
    context.applicationContext,
    BePresentDatabase::class.java,
    "bepresent.db"
).fallbackToDestructiveMigration().build()
```
