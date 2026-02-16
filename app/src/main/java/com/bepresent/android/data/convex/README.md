# Convex Module

Cloud database integration using Convex with Auth0 authentication and offline-first sync.

## Files

```
convex/
├── ConvexManager.kt     # Auth and client management
├── SyncManager.kt       # Queue-based sync orchestration
├── SyncPayloads.kt      # Data transfer objects
└── SyncWorker.kt        # WorkManager background sync
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      Local Data Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │ Room DB      │  │ DataStore    │  │ Feature Managers     │   │
│  │ (sessions,   │  │ (prefs)      │  │ (Session, Intention) │   │
│  │ intentions)  │  │              │  │                      │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
└─────────┼─────────────────┼─────────────────────┼───────────────┘
          │                 │                     │
          └─────────────────┼─────────────────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │   SyncManager   │
                   │  (enqueue ops)  │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  SyncQueueDao   │
                   │  (Room table)   │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │   SyncWorker    │
                   │  (WorkManager)  │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  ConvexManager  │
                   │  (client + auth)│
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  Convex Cloud   │
                   │  (mutations)    │
                   └─────────────────┘
```

## ConvexManager

Singleton managing Convex client and Auth0 authentication state.

### Auth State Machine

```kotlin
sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
}
```

### Key Methods

| Method | Description |
|--------|-------------|
| `login()` | Initiates Auth0 login flow |
| `logout()` | Clears auth state |
| `loginFromCache()` | Restores session from cached credentials |

### Configuration

Uses BuildConfig values:
- `AUTH0_CLIENT_ID`
- `AUTH0_DOMAIN`
- `CONVEX_URL`

## SyncManager

Orchestrates offline-first sync by enqueueing operations to a local queue.

### Sync Types

| Type | Payload | Convex Mutation |
|------|---------|-----------------|
| `TYPE_SESSION` | `SessionSyncPayload` | `stats:syncSession` |
| `TYPE_DAILY_STATS` | `DailyStatsSyncPayload` | `stats:syncDailyStats` |
| `TYPE_INTENTIONS` | `IntentionsSyncPayload` | `stats:syncIntentions` |

### Enqueue Methods

```kotlin
suspend fun enqueueSessionSync(session: PresentSession)
suspend fun enqueueDailyStatsSync(date: LocalDate)
suspend fun enqueueIntentionsSync()
```

### Process Queue

```kotlin
suspend fun processQueue() {
    if (!convexManager.isAuthenticated) return
    
    syncQueueDao.deleteFailedItems()  // Clean up items with >10 retries
    
    for (item in syncQueueDao.getAll()) {
        try {
            // Decode payload, call Convex mutation
            syncQueueDao.delete(item)
        } catch (e: Exception) {
            syncQueueDao.incrementRetry(item.id)
        }
    }
}
```

## SyncPayloads

Kotlinx Serialization data classes for JSON encoding:

```kotlin
@Serializable
data class SessionSyncPayload(
    val localSessionId: String,
    val name: String,
    val goalDurationMinutes: Int,
    val state: String,
    val earnedXp: Int,
    val startedAt: Long,
    val endedAt: Long?
)

@Serializable
data class DailyStatsSyncPayload(
    val date: String,           // ISO format "2024-01-15"
    val totalXp: Int,
    val totalCoins: Int,
    val maxStreak: Int,
    val sessionsCompleted: Int,
    val totalFocusMinutes: Int
)

@Serializable
data class IntentionsSyncPayload(
    val intentions: List<IntentionSnapshotPayload>
)
```

## SyncWorker

WorkManager-based background sync with network constraints.

### Scheduling

| Type | Interval | Constraint |
|------|----------|------------|
| Periodic | Every 15 minutes | Network connected |
| Immediate | On-demand | Network connected |

### Usage

```kotlin
// Schedule recurring sync (typically at app startup)
SyncWorker.schedulePeriodic(context)

// Trigger immediate sync (after session completion)
SyncWorker.triggerImmediateSync(context)
```

## Sync Queue Table

```sql
CREATE TABLE sync_queue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,        -- "session", "dailyStats", "intentions"
    payload TEXT NOT NULL,     -- JSON encoded payload
    createdAt INTEGER NOT NULL,
    retryCount INTEGER DEFAULT 0
)
```

Items are deleted after successful sync or after exceeding 10 retry attempts.

## Error Handling

- Sync failures increment `retryCount`
- Items with >10 retries are deleted to prevent queue buildup
- Network failures trigger WorkManager retry policy
- Auth failures skip sync (requires re-login)
