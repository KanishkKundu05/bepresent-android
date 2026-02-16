# Data Layer

Local persistence and API layer connecting feature managers to storage backends.

## Directory Structure

```
data/
├── db/                 # Room database (local persistence)
│   ├── BePresentDatabase.kt
│   ├── AppIntention.kt / AppIntentionDao.kt
│   ├── PresentSession.kt / PresentSessionDao.kt
│   └── SyncQueueItem.kt / SyncQueueDao.kt
├── datastore/          # Preferences (key-value storage)
│   └── PreferencesManager.kt
├── convex/             # Cloud sync (see convex/README.md)
│   └── ...
└── usage/              # Android Usage Stats API
    └── UsageStatsRepository.kt
```

## API Layer Architecture

The data layer serves as the bridge between feature managers and storage:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        UI Layer (ViewModels)                         │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Feature Layer (Managers)                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │ SessionManager  │  │ IntentionManager│  │ PermissionManager   │  │
│  └────────┬────────┘  └────────┬────────┘  └─────────────────────┘  │
└───────────┼────────────────────┼────────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Data Layer (API)                              │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      Room DAOs                                 │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌───────────────┐  │  │
│  │  │PresentSessionDao│  │AppIntentionDao  │  │SyncQueueDao   │  │  │
│  │  │ - getActive()   │  │ - getAll()      │  │ - insert()    │  │  │
│  │  │ - upsert()      │  │ - upsert()      │  │ - delete()    │  │  │
│  │  │ - observe()     │  │ - getByPackage()│  │ - getAll()    │  │  │
│  │  └─────────────────┘  └─────────────────┘  └───────────────┘  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                   PreferencesManager                           │  │
│  │  - totalXp, totalCoins (gamification)                         │  │
│  │  - activeSessionId (cross-process coordination)               │  │
│  │  - streakFreezeAvailable (weekly perk)                        │  │
│  │  - onboardingCompleted (first-run flag)                       │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                   UsageStatsRepository                         │  │
│  │  - getTotalScreenTimeToday()                                  │  │
│  │  - getPerAppScreenTime()                                      │  │
│  │  - detectForegroundApp()                                      │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                   SyncManager                                  │  │
│  │  - enqueueSessionSync()                                       │  │
│  │  - enqueueDailyStatsSync()                                    │  │
│  │  - enqueueIntentionsSync()                                    │  │
│  │  - processQueue() → ConvexManager → Convex Cloud              │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## Room Database

### Entities

**PresentSession**
```kotlin
@Entity(tableName = "present_sessions")
data class PresentSession(
    @PrimaryKey val id: String,
    val name: String,
    val goalDurationMinutes: Int,
    val beastMode: Boolean,
    val state: String,           // idle, active, goalReached, completed, gaveUp, canceled
    val blockedPackages: String, // JSON array
    val startedAt: Long?,
    val endedAt: Long?,
    val goalReachedAt: Long?,
    val earnedXp: Int,
    val earnedCoins: Int,
    val createdAt: Long
)
```

**AppIntention**
```kotlin
@Entity(tableName = "app_intentions")
data class AppIntention(
    @PrimaryKey val id: String,
    val packageName: String,
    val appName: String,
    val allowedOpensPerDay: Int,
    val timePerOpenMinutes: Int,
    val totalOpensToday: Int,
    val streak: Int,
    val currentlyOpen: Boolean,
    val openedAt: Long?,
    val lastResetDate: String?
)
```

**SyncQueueItem**
```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,      // session, dailyStats, intentions
    val payload: String,   // JSON
    val createdAt: Long,
    val retryCount: Int
)
```

### Migrations

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE sync_queue ...")
    }
}
```

## PreferencesManager

DataStore-based key-value storage for app preferences:

| Key | Type | Purpose |
|-----|------|---------|
| `onboarding_completed` | Boolean | Skip onboarding on subsequent launches |
| `total_xp` | Int | Cumulative experience points |
| `total_coins` | Int | Cumulative reward coins |
| `streak_freeze_available` | Boolean | Weekly perk to protect streaks |
| `last_freeze_grant_date` | String | Prevents duplicate weekly grants |
| `active_session_id` | String? | Coordinates session state across app/service |

### Flow-based Reads

```kotlin
val totalXp: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_XP] ?: 0 }
```

### Suspend Writes

```kotlin
suspend fun addXpAndCoins(xp: Int, coins: Int)
suspend fun setActiveSessionId(sessionId: String?)
```

## UsageStatsRepository

Wraps Android `UsageStatsManager` for screen time data:

| Method | Returns | Description |
|--------|---------|-------------|
| `getTotalScreenTimeToday()` | `Long` | Total foreground time in ms |
| `getPerAppScreenTime()` | `List<AppUsageInfo>` | Per-app breakdown sorted by usage |
| `detectForegroundApp()` | `String?` | Current foreground app package |

Used by:
- `DashboardViewModel` - Display screen time card
- `MonitoringService` - Detect blocked app launches

## Dependency Injection

All data layer classes are `@Singleton` and `@Inject` constructor, provided via Hilt modules:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BePresentDatabase
    
    @Provides
    fun provideSessionDao(db: BePresentDatabase): PresentSessionDao
    // ...
}
```

## Data Flow Examples

### Session Completion

```
SessionManager.complete(sessionId)
    │
    ├── sessionDao.upsert(updated)           // Persist to Room
    ├── preferencesManager.addXpAndCoins()   // Update rewards
    ├── preferencesManager.setActiveSessionId(null)
    ├── syncManager.enqueueSessionSync()     // Queue for cloud sync
    └── SyncWorker.triggerImmediateSync()    // Trigger background sync
```

### Daily Reset

```
DailyResetWorker.doWork()
    │
    ├── intentionDao.getAllOnce()            // Read all intentions
    ├── preferencesManager.getStreakFreezeAvailableOnce()
    │
    ├── For each intention:
    │   └── intentionDao.upsert(updated)     // Reset counters, update streak
    │
    ├── syncManager.enqueueDailyStatsSync()  // Queue stats
    └── syncManager.enqueueIntentionsSync()  // Queue intention snapshots
```
