# BePresent Android — MVP Single-Screen Spec

A scoped-down specification for the first buildable version of BePresent on Android: a **single scrollable dashboard** with two core features — **App Intentions** and **Blocking Sessions**. Fully offline, no auth, no backend.

---

## 1. MVP Scope

### In Scope
- Single dashboard screen (Jetpack Compose)
- App Intentions — per-app daily open limits with shield/blocked screen
- Blocking Sessions — timed focus sessions that block selected apps
- Foreground service for app monitoring (UsageStats polling at 1s)
- Screen time reading via UsageStatsManager
- Permission onboarding flow (step-by-step)
- Local persistence — Room (entities) + DataStore (preferences/flags)
- App picker (PackageManager-based, bottom sheet)
- Shield screen (BlockedAppActivity) — full-screen Activity approach
- XP/coins calculated locally on session completion
- Streak tracking per intention with daily midnight reset
- Foreground service notification with chronometer during sessions
- BOOT_COMPLETED receiver for service persistence

### Out of Scope
- Auth / accounts / backend sync
- Social features / leaderboard / accountability partners
- Home screen & lock screen widgets
- Scheduled/recurring sessions
- Analytics / experiments / feature flags
- Subscriptions / paywall (streak freeze is always available)
- AccessibilityService (UsageStats polling only for MVP)
- Device Admin / Device Owner
- Web domain blocking
- Daily quest / daily review
- Screen time goal & lives system
- Witty screen time threshold notifications

---

## 2. Main Screen Layout

A single scrollable dashboard. Top to bottom:

### 2a. Header Bar
```
┌──────────────────────────────────────┐
│  BePresent          🔥 14    ⭐ 250  │
│                    streak     XP     │
└──────────────────────────────────────┘
```
- App name/logo on the left
- Streak count (longest active intention streak, local)
- XP count (cumulative session XP, local)

### 2b. Screen Time Card
```
┌──────────────────────────────────────┐
│         ╭───────╮                    │
│         │ 2h 34m│  ← circular       │
│         │       │    progress ring   │
│         ╰───────╯                    │
│                                      │
│  [Instagram 45m] [TikTok 32m] ...   │
│  ← horizontally scrollable chips    │
└──────────────────────────────────────┘
```
- Circular progress indicator showing total screen time today
- Progress ring fills based on a reference max (e.g. 8 hours = full)
- Per-app usage chips — horizontal scroll, sorted by usage descending
- Each chip: app icon + name + time in foreground today
- Data source: `UsageStatsManager.queryUsageStats(INTERVAL_DAILY, ...)`

### 2c. App Intentions Row
```
┌──────────────────────────────────────┐
│  Your Intentions              + Add  │
│                                      │
│  ┌─────────┐ ┌─────────┐ ┌────────┐ │
│  │📱 Insta │ │📱TikTok│ │📱 X    │ │
│  │ 1/3     │ │ 0/2    │ │ 2/5    │ │
│  │ 🔥 14   │ │ 🔥 7   │ │ 🔥 3   │ │
│  └─────────┘ └─────────┘ └────────┘ │
│  ← horizontally scrollable cards    │
└──────────────────────────────────────┘
```
- Section header: "Your Intentions" with "+ Add" button
- Horizontal scroll of intention cards
- Each card: app icon, app name, opens used/allowed (e.g. "1/3"), streak with flame
- Tapping a card opens intention detail/edit
- "+ Add" opens the app picker to create a new intention

### 2d. Start Blocking Session CTA
```
┌──────────────────────────────────────┐
│                                      │
│   ┌──────────────────────────────┐   │
│   │    🛡️ Start Blocking Session │   │
│   └──────────────────────────────┘   │
│                                      │
└──────────────────────────────────────┘
```
- Prominent button, sticky at bottom or at end of scroll
- Tapping opens session configuration bottom sheet

---

## 3. App Intentions Feature Spec

### 3a. Overview

App Intentions let users set daily open limits on specific apps. The app is shielded by default; the user must go through the shield to open it, consuming one of their daily opens. A timed window lets them use the app for a set duration before it re-shields.

### 3b. Creating an Intention

1. User taps "+ Add" on the intentions row
2. App picker bottom sheet opens (see Section 5)
3. User selects one app
4. Configuration dialog:
   - **Allowed opens per day**: slider or stepper, range 1–10, default 3
   - **Time per open**: slider or stepper, range 1–30 minutes, default 5 minutes
5. Save → Room entity created → app added to blocked list immediately

### 3c. Data Model

```kotlin
@Entity(tableName = "app_intentions")
data class AppIntention(
    @PrimaryKey
    val id: String,                       // UUID
    val packageName: String,              // e.g. "com.instagram.android"
    val appName: String,                  // cached display name
    val allowedOpensPerDay: Int,          // e.g. 3
    val timePerOpenMinutes: Int,          // e.g. 5
    val totalOpensToday: Int = 0,         // incremented on each open
    val streak: Int = 0,                  // consecutive days within limit
    val lastResetDate: String = "",       // ISO date "2026-02-14"
    val currentlyOpen: Boolean = false,   // is the timed window active?
    val openedAt: Long? = null,           // epoch millis of current open start
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3d. Full Lifecycle

```
[Intention created — app is shielded]
        │
User taps blocked app
        │
Monitoring service detects → launches BlockedAppActivity (intention variant)
        │
Shield shows: "Open Instagram?"
              "1/3 Opens Today"
              "🔥 14 Day Streak"
              [Nevermind]  [Open Instagram]
        │
        ├── "Nevermind" → navigate to home, app stays blocked
        │
        └── "Open Instagram" →
              │
              1. Increment totalOpensToday (1→2)
              2. Set currentlyOpen = true, openedAt = now
              3. Remove packageName from blocked list temporarily
              4. Schedule re-block alarm: AlarmManager.setAlarmClock()
                 at (now + timePerOpenMinutes)
              5. Show notification: "Instagram — open for 5 minutes"
              6. Finish BlockedAppActivity → user sees the actual app
              │
         [User uses app for timePerOpenMinutes]
              │
         30 seconds before expiry →
              Notification: "Closing Instagram in 30 seconds"
              │
         Timer expires → alarm fires →
              │
              1. Set currentlyOpen = false, openedAt = null
              2. Add packageName back to blocked list
              3. If app is currently in foreground →
                 launch BlockedAppActivity on top
              4. Notification: "Instagram time is up"
              │
         [App is shielded again]
```

### 3e. Daily Reset (Midnight)

Via WorkManager `PeriodicWorkRequest` (1 day, initial delay until midnight):

```
For each AppIntention:
  if totalOpensToday <= allowedOpensPerDay:
    streak += 1
  else if streakFreezeAvailable:
    streak += 1  (freeze consumed)
  else:
    streak = 0  (broken)
  totalOpensToday = 0
  lastResetDate = today
  currentlyOpen = false
  openedAt = null
```

### 3f. Streak Freeze

- 1 freeze per week, granted every Monday
- Protects ALL intention streaks for the day it's used
- In MVP: always available (no Pro gate)
- Stored in DataStore: `streakFreezeAvailable: Boolean`, `lastFreezeGrantDate: String`
- Auto-grant logic in the daily reset worker: if today is Monday, set `streakFreezeAvailable = true`

### 3g. Editing / Deleting Intentions

- Tap an intention card → detail sheet with current stats
- Edit: change allowed opens or time per open
- Delete: remove intention, remove from blocked list, delete Room entity

### 3h. Over-Limit Behavior

When all daily opens are exhausted (totalOpensToday >= allowedOpensPerDay):
- Shield still shows but with stronger messaging: "You've used all 3 opens today"
- User can still tap "Open Anyway" (soft enforcement — we don't hard-block)
- "Open Anyway" increments totalOpensToday beyond the limit (streak will break at midnight unless freeze is active)
- This matches iOS behavior: the shield is an intervention, not a prison

---

## 4. Blocking Session Feature Spec

### 4a. Overview

A blocking session is a timed focus commitment. The user selects apps to block, sets a goal duration, and starts the session. Blocked apps show a shield when opened. XP and coins are awarded on completion.

### 4b. Session Configuration

Bottom sheet triggered by the "Start Blocking Session" CTA:

| Setting | Details |
|---|---|
| **Session name** | Text input, optional, default "Focus Session" |
| **Goal duration** | Picker: 5, 10, 15, 20, 30, 45, 60, 90, 120 minutes |
| **Mode** | Block List only for MVP (block specific apps) |
| **Apps to block** | Opens app picker (multi-select) |
| **Beast Mode** | Toggle — if on, "Give Up" is disabled |

### 4c. Data Model

```kotlin
@Entity(tableName = "present_sessions")
data class PresentSession(
    @PrimaryKey
    val id: String,                       // UUID
    val name: String,
    val goalDurationMinutes: Int,
    val beastMode: Boolean = false,
    val state: String = "idle",           // idle | active | goalReached | completed | gaveUp | canceled
    val blockedPackages: String,          // JSON array of package names
    val startedAt: Long? = null,
    val goalReachedAt: Long? = null,
    val endedAt: Long? = null,
    val earnedXp: Int = 0,
    val earnedCoins: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "present_session_actions")
data class PresentSessionAction(
    @PrimaryKey
    val id: String,                       // UUID
    val sessionId: String,                // FK to PresentSession
    val action: String,                   // "start" | "giveUp" | "cancel" | "goalReached" | "complete" | "extend"
    val timestamp: Long = System.currentTimeMillis()
)
```

### 4d. Session State Machine

```
        ┌─────────────┐
        │    idle      │
        └──────┬──────┘
               │ start()
               ▼
        ┌─────────────┐
        │   active     │ ←── blocking is ON, timer running
        └──┬───┬───┬──┘
           │   │   │
  giveUp() │   │   │ goalDuration elapsed
           │   │   │
           │   │   ▼
           │   │ ┌─────────────┐
           │   │ │ goalReached  │ ←── shield changes to celebration
           │   │ └──┬───────┬──┘
           │   │    │       │
           │   │    │       │ complete()
           │   │    │       ▼
           │   │    │  ┌──────────┐
           │   │    │  │completed │ ←── XP awarded, session saved
           │   │    │  └──────────┘
           │   │    │
           │   │    │ user keeps going (extra time)
           │   │    │ → stays in goalReached, blocking remains ON
           │   │    │ → complete() whenever user taps "End Session"
           │   │
           │   │ cancel() (within first 10 seconds)
           │   ▼
           │  ┌──────────┐
           │  │ canceled  │ ←── no penalty, no XP
           │  └──────────┘
           │
           ▼
        ┌──────────┐
        │ gaveUp   │ ←── partial XP (none for MVP)
        └──────────┘
```

### 4e. State Transitions

| Current State | Action | New State | Side Effects |
|---|---|---|---|
| idle | start() | active | Start foreground service, add apps to blocked list, start notification with chronometer, schedule goalReached alarm |
| active | cancel() | canceled | Only if < 10s elapsed. Stop service, clear blocked list, remove notification |
| active | giveUp() | gaveUp | Only if beast mode is OFF. Stop blocking, award 0 XP, save session |
| active | goalReached() | goalReached | Triggered by alarm. Update notification ("Goal reached!"), shield shows celebration variant. Blocking remains ON |
| goalReached | complete() | completed | Stop blocking, calculate XP/coins, save session, remove notification |

### 4f. XP / Coins Table

| Goal Duration | XP | Coins |
|---|---|---|
| ≤ 15 min | 3 | 3 |
| ≤ 30 min | 5 | 5 |
| ≤ 45 min | 8 | 8 |
| ≤ 60 min | 10 | 10 |
| ≤ 90 min | 15 | 15 |
| ≤ 120 min | 25 | 25 |

XP and coins are awarded only on `completed` state. Stored in DataStore as cumulative totals.

### 4g. Session + Intention Interaction

If both a session and an intention apply to the same app:
- **Session takes priority** — show the session shield variant
- The intention's open count is NOT affected by session blocks
- After the session ends, the intention shield resumes normally

---

## 5. App Picker

### 5a. Overview

A reusable bottom sheet for selecting apps. Used by both intention creation (single-select) and session configuration (multi-select).

### 5b. Data Source

```kotlin
val pm = context.packageManager
val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
val apps = pm.queryIntentActivities(intent, 0)
    .map { resolveInfo ->
        InstalledApp(
            packageName = resolveInfo.activityInfo.packageName,
            label = resolveInfo.loadLabel(pm).toString(),
            icon = resolveInfo.loadIcon(pm)  // Drawable
        )
    }
    .filter { it.packageName != context.packageName } // exclude self
    .sortedBy { it.label.lowercase() }
```

### 5c. UI

```
┌──────────────────────────────────────┐
│  Select Apps                    Done │
│  ┌──────────────────────────────┐    │
│  │ 🔍 Search apps...           │    │
│  └──────────────────────────────┘    │
│                                      │
│  [✓] 📱 Instagram                    │
│  [ ] 📱 Messages                     │
│  [ ] 📱 Snapchat                     │
│  [✓] 📱 TikTok                       │
│  [ ] 📱 Twitter / X                  │
│  [ ] 📱 YouTube                      │
│  ...                                 │
└──────────────────────────────────────┘
```

- Bottom sheet with drag handle
- Search bar at top — filters by app label (case-insensitive)
- Each row: app icon (loaded from PackageManager), app label, checkbox
- Single-select mode for intentions, multi-select for sessions
- "Done" button at top right — returns selected package name(s)
- Lazy column for performance (100+ apps)

### 5d. Permission

`QUERY_ALL_PACKAGES` declared in manifest. Required on Android 11+ to see all installed apps. Needs Play Store justification: "Display installed apps for user to select which to block during focus sessions."

---

## 6. Shield / Blocked Screen (BlockedAppActivity)

### 6a. Approach

Full-screen Activity (not overlay). This is the recommended approach per the critical considerations doc — more reliable than SYSTEM_ALERT_WINDOW, handles navigation properly, works with Compose.

### 6b. Manifest

```xml
<activity
    android:name=".features.blocking.BlockedAppActivity"
    android:exported="false"
    android:excludeFromRecents="true"
    android:launchMode="singleTask"
    android:taskAffinity=""
    android:theme="@style/Theme.BePresent.FullScreen" />
```

- `excludeFromRecents` — don't show in recent apps (prevents user from switching back to the blocked app via this entry)
- `singleTask` — only one instance
- empty `taskAffinity` — launches in its own task, separate from the blocked app's task

### 6c. Launch

From the monitoring service when a blocked app is detected in foreground:

```kotlin
val intent = Intent(context, BlockedAppActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
    putExtra("blocked_package", detectedPackageName)
    putExtra("shield_type", shieldType) // "session" | "intention" | "goalReached"
}
context.startActivity(intent)
```

### 6d. Shield Variants

#### Session Active
```
┌──────────────────────────────────────┐
│                                      │
│            🛡️                        │
│                                      │
│      "Focus Session"                 │
│      Session name                    │
│                                      │
│      ┌─────────────────┐             │
│      │   Be Present    │             │
│      └─────────────────┘             │
│                                      │
│      Unlock?                         │
│      (shows unlock instructions)     │
│                                      │
└──────────────────────────────────────┘
```
- Primary action: "Be Present" → navigates to home screen
- Secondary: "Unlock?" → shows text explaining how to end the session in BePresent app (or disabled if beast mode)

#### Goal Reached
```
┌──────────────────────────────────────┐
│                                      │
│            🎉                        │
│                                      │
│   "Session Goal Reached!"            │
│   "+10 XP"                           │
│                                      │
│      ┌─────────────────┐             │
│      │    Complete      │             │
│      └─────────────────┘             │
│                                      │
│      Stay Present                    │
│                                      │
└──────────────────────────────────────┘
```
- Primary: "Complete" → ends session, awards XP, goes home
- Secondary: "Stay Present" → goes home, session continues (extra time)

#### App Intention
```
┌──────────────────────────────────────┐
│                                      │
│         📱 Instagram                 │
│                                      │
│      "Open Instagram?"               │
│      "1/3 Opens Today"               │
│      "🔥 14 Day Streak"             │
│                                      │
│      ┌─────────────────┐             │
│      │   Nevermind      │             │
│      └─────────────────┘             │
│                                      │
│      Open Instagram                  │
│      (for 5 minutes)                 │
│                                      │
└──────────────────────────────────────┘
```
- Primary: "Nevermind" → navigates to home, app stays blocked
- Secondary: "Open Instagram" → starts timed open window, finishes this Activity

#### Intention — Over Limit
Same as intention variant but with stronger messaging:
- "You've used all 3 opens today"
- "Opening will break your 🔥 14 day streak" (unless freeze active)
- Secondary becomes "Open Anyway"

#### Streak Freeze Active
Same as intention variant with banner:
- "Streak Freeze Active ❄️" — opens don't count against streak today

### 6e. Navigation

- **Back button** → navigate to home (never reveal the blocked app underneath)
- **Recents/task switcher** → if user switches away and goes back to the blocked app, the monitoring service re-launches BlockedAppActivity

```kotlin
// In BlockedAppActivity
override fun onBackPressed() {
    navigateHome()
}

private fun navigateHome() {
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(homeIntent)
    finish()
}
```

---

## 7. Monitoring Service

### 7a. Overview

A foreground service that polls UsageStatsManager at 1-second intervals to detect which app is in the foreground. If the foreground app is on the blocked list, it launches BlockedAppActivity.

### 7b. Blocked List

The blocked list is the **union** of:
1. **Session blocked apps** — packages selected for the active session (empty if no session active)
2. **Intention blocked apps** — all intention packages where `currentlyOpen == false`

```kotlin
fun getBlockedPackages(): Set<String> {
    val sessionBlocked = if (activeSession != null) {
        activeSession.blockedPackages.toSet()
    } else emptySet()

    val intentionBlocked = intentionRepository.getAll()
        .filter { !it.currentlyOpen }
        .map { it.packageName }
        .toSet()

    return sessionBlocked + intentionBlocked
}
```

When both apply to the same app, session shield takes priority (see Section 4g).

### 7c. Polling Loop

```kotlin
class MonitoringService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMonitoringNotification())
        startPolling()
        return START_STICKY
    }

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                val foregroundPackage = detectForegroundApp()
                if (foregroundPackage != null && foregroundPackage in getBlockedPackages()) {
                    val shieldType = determineShieldType(foregroundPackage)
                    launchBlockedActivity(foregroundPackage, shieldType)
                }
                delay(1000) // 1-second poll interval
            }
        }
    }

    private fun detectForegroundApp(): String? {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 5000 // look back 5 seconds for reliability
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
}
```

### 7d. Persistence

- `START_STICKY` — OS restarts service if killed (1–10 second gap)
- `BOOT_COMPLETED` receiver — restarts service on reboot

```kotlin
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if there's an active session or any intentions
            // If so, start the monitoring service
            MonitoringService.start(context)
        }
    }
}
```

Manifest:
```xml
<receiver
    android:name=".service.BootCompletedReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### 7e. Service Notification

A persistent low-priority notification while monitoring is active:
- Title: "BePresent is active"
- Text: "Monitoring your app usage" (or "Focus session: 23m remaining" during a session)
- Cannot be dismissed (foreground service requirement)
- Tapping opens the main dashboard

### 7f. Service Lifecycle

- **Start**: when a session starts OR when any intention exists
- **Stop**: when session ends AND no intentions exist
- **Resume on boot**: if session was active or intentions exist (check Room/DataStore)
- **Permission check**: on every service start, verify UsageStats permission is granted. If not, show a notification prompting the user to re-grant.

### 7g. The "Flash" Problem

With 1-second polling, the blocked app is visible for up to ~1 second before the shield covers it. This is expected and acceptable for MVP. Mitigations for later versions:
- AccessibilityService (reduces to ~100ms)
- Hybrid overlay + Activity approach

---

## 8. Permissions & Onboarding

### 8a. Required Permissions

| Permission | Grant Method | What It Enables | Required? |
|---|---|---|---|
| `PACKAGE_USAGE_STATS` | Settings > Usage Access | Screen time reading, foreground app detection | Yes — core |
| `SYSTEM_ALERT_WINDOW` | Settings > Display Over Other Apps | Not used for MVP (Activity approach instead) | No |
| `POST_NOTIFICATIONS` | Runtime dialog (Android 13+) | Session notifications, intention timer alerts | Yes |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | System dialog | Keep monitoring service alive | Yes |
| `QUERY_ALL_PACKAGES` | Manifest (auto-granted) | List installed apps in picker | Yes (manifest only) |
| `RECEIVE_BOOT_COMPLETED` | Manifest (auto-granted) | Restart service on reboot | Yes (manifest only) |
| `FOREGROUND_SERVICE` | Manifest (auto-granted) | Run monitoring foreground service | Yes (manifest only) |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Manifest (API 34+) | Foreground service type for monitoring | Yes (manifest only) |
| `USE_EXACT_ALARM` | Auto-granted | Intention re-block alarms, session goal alarm | Yes (manifest only) |

**Note:** `SYSTEM_ALERT_WINDOW` is NOT required for MVP since we use the full-screen Activity approach for the shield. The foreground service can launch Activities via `FLAG_ACTIVITY_NEW_TASK`.

### 8b. Onboarding Flow

Step-by-step screens, shown on first launch:

```
Step 1: Welcome
  "BePresent helps you be intentional with your phone"
  [Get Started]

Step 2: Usage Access (CRITICAL)
  "BePresent needs to see which apps you use to help you set limits"
  [Grant Access] → opens Settings.ACTION_USAGE_ACCESS_SETTINGS
  → on return, verify with AppOpsManager
  → if not granted, show "This permission is required" with retry

Step 3: Notifications
  "Get notified when your app time is up and sessions complete"
  [Enable Notifications] → requestPermission(POST_NOTIFICATIONS) (Android 13+)
  → on older versions, skip this step

Step 4: Battery Optimization
  "Keep BePresent running reliably in the background"
  [Disable Battery Optimization] → ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
  → also show OEM-specific instructions (see 8c)

Step 5: All Set
  "You're ready to be present!"
  [Open Dashboard]
```

Store completion in DataStore: `onboardingCompleted: Boolean`

### 8c. OEM Battery Instructions

Based on `Build.MANUFACTURER`, show an additional card in Step 4:

| Manufacturer | Instructions |
|---|---|
| Xiaomi / Redmi | Settings > Apps > Manage Apps > BePresent > Battery Saver > No restrictions |
| Huawei / Honor | Settings > Apps > BePresent > Battery > App Launch > Manual (all toggles ON) |
| Samsung | Settings > Battery > Background usage limits > Never sleeping apps > Add BePresent |
| Oppo / Realme | Settings > Battery > App Quick Freeze > disable for BePresent |
| OnePlus | Settings > Battery > Battery Optimization > BePresent > Don't optimize |
| Vivo | Settings > Battery > Background Power Consumption > BePresent > Off |

### 8d. Permission Health Check

On every app launch (not just first time):
1. Check `PACKAGE_USAGE_STATS` via `AppOpsManager`
2. Check `PowerManager.isIgnoringBatteryOptimizations()`
3. Check `NotificationManagerCompat.areNotificationsEnabled()`
4. If any critical permission missing → show a banner on the dashboard: "BePresent can't monitor apps — tap to fix"
5. Tapping the banner → re-opens the relevant permission Settings screen

---

## 9. Data Models

### 9a. Room Database

**Database name:** `bepresent.db`

**Entities:**

1. `AppIntention` — see Section 3c
2. `PresentSession` — see Section 4c
3. `PresentSessionAction` — see Section 4c

**DAOs:**

```kotlin
@Dao
interface AppIntentionDao {
    @Query("SELECT * FROM app_intentions")
    fun getAll(): Flow<List<AppIntention>>

    @Query("SELECT * FROM app_intentions WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): AppIntention?

    @Query("SELECT * FROM app_intentions WHERE id = :id")
    suspend fun getById(id: String): AppIntention?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(intention: AppIntention)

    @Delete
    suspend fun delete(intention: AppIntention)

    @Query("UPDATE app_intentions SET totalOpensToday = totalOpensToday + 1 WHERE id = :id")
    suspend fun incrementOpens(id: String)

    @Query("UPDATE app_intentions SET currentlyOpen = :open, openedAt = :openedAt WHERE id = :id")
    suspend fun setOpenState(id: String, open: Boolean, openedAt: Long?)
}

@Dao
interface PresentSessionDao {
    @Query("SELECT * FROM present_sessions WHERE state IN ('active', 'goalReached') LIMIT 1")
    suspend fun getActiveSession(): PresentSession?

    @Query("SELECT * FROM present_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<PresentSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: PresentSession)

    @Insert
    suspend fun insertAction(action: PresentSessionAction)
}
```

### 9b. DataStore (Preferences)

**File:** `bepresent_prefs.preferences_pb`

| Key | Type | Purpose |
|---|---|---|
| `onboarding_completed` | Boolean | Whether onboarding flow was completed |
| `total_xp` | Int | Cumulative XP across all sessions |
| `total_coins` | Int | Cumulative coins across all sessions |
| `streak_freeze_available` | Boolean | Whether a streak freeze is available this week |
| `last_freeze_grant_date` | String | ISO date of last Monday freeze was granted |
| `active_session_id` | String? | ID of the currently active session (for quick lookup) |

---

## 10. Technical Architecture

### 10a. Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose (Material 3) |
| Navigation | Single Activity — Compose state (no Navigation component needed for single screen) |
| DI | Hilt |
| State management | ViewModel + StateFlow |
| Local DB | Room |
| Preferences | DataStore (Preferences) |
| Background | Foreground Service (monitoring), WorkManager (daily reset), AlarmManager (intention timers, session goal) |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 26 (Android 8) |
| Target SDK | 34 (Android 14) |

### 10b. Module Structure (single module for MVP)

```
app/
├── src/main/java/com/bepresent/android/
│   ├── BePresentApp.kt              // Application class + Hilt
│   ├── MainActivity.kt              // Single activity, hosts Compose
│   ├── ui/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt   // Main scrollable screen
│   │   │   └── DashboardViewModel.kt
│   │   ├── components/
│   │   │   ├── ScreenTimeCard.kt
│   │   │   ├── IntentionRow.kt
│   │   │   ├── IntentionCard.kt
│   │   │   └── SessionCta.kt
│   │   ├── picker/
│   │   │   └── AppPickerSheet.kt
│   │   ├── session/
│   │   │   └── SessionConfigSheet.kt
│   │   ├── intention/
│   │   │   └── IntentionConfigSheet.kt
│   │   ├── onboarding/
│   │   │   └── OnboardingScreen.kt
│   │   └── theme/
│   │       └── Theme.kt
│   ├── features/
│   │   ├── blocking/
│   │   │   ├── BlockedAppActivity.kt
│   │   │   └── ShieldScreen.kt      // Compose UI for shield variants
│   │   ├── intentions/
│   │   │   ├── IntentionManager.kt
│   │   │   └── DailyResetWorker.kt
│   │   └── sessions/
│   │       ├── SessionManager.kt
│   │       └── SessionStateMachine.kt
│   ├── service/
│   │   ├── MonitoringService.kt
│   │   ├── BootCompletedReceiver.kt
│   │   └── IntentionAlarmReceiver.kt
│   ├── data/
│   │   ├── db/
│   │   │   ├── BePresentDatabase.kt
│   │   │   ├── AppIntentionDao.kt
│   │   │   └── PresentSessionDao.kt
│   │   ├── datastore/
│   │   │   └── PreferencesManager.kt
│   │   └── usage/
│   │       └── UsageStatsRepository.kt
│   └── permissions/
│       ├── PermissionManager.kt
│       └── OemBatteryGuide.kt
```

### 10c. Key Architectural Decisions

1. **Full-screen Activity for shield** (not overlay) — avoids `SYSTEM_ALERT_WINDOW` permission, more reliable navigation handling, Compose-native
2. **UsageStats polling** (not AccessibilityService) — Play Store safe, no special review, acceptable 1s latency for MVP
3. **Single Activity** — dashboard is the only real "screen"; everything else is bottom sheets or BlockedAppActivity (separate task)
4. **Room for structured data** (intentions, sessions) + **DataStore for flags/prefs** — standard Android pattern
5. **AlarmManager for timed events** (intention re-block, session goal reached) — most reliable, survives Doze
6. **WorkManager for daily reset** — survives process death, handles rescheduling
7. **No server sync** — all data is local, simplifies everything dramatically

---

## 11. Build Order

Each step builds on the previous. No circular dependencies.

### Step 1: Project Skeleton
- Android project with Compose, Hilt, Room, DataStore, Material 3
- `BePresentApp.kt` Application class with `@HiltAndroidApp`
- `MainActivity.kt` with empty Compose scaffold
- `BePresentDatabase.kt` with empty entity list
- `PreferencesManager.kt` DataStore wrapper
- Build & run: blank screen with app bar

### Step 2: Permission Manager + Onboarding
- `PermissionManager.kt` — check/request Usage Access, Notifications, Battery
- `OemBatteryGuide.kt` — manufacturer-specific instructions
- `OnboardingScreen.kt` — step-by-step permission flow
- DataStore flag: `onboarding_completed`
- Build & run: onboarding flow → grants permissions → shows empty dashboard

### Step 3: UsageStatsManager Wrapper
- `UsageStatsRepository.kt`:
  - `getTotalScreenTimeToday(): Long` (millis)
  - `getPerAppScreenTime(): Map<String, Long>`
  - `detectForegroundApp(): String?`
- Build & run: log screen time data to verify UsageStats works

### Step 4: App Picker
- `AppPickerSheet.kt` — bottom sheet with search, app icons, multi-select/single-select mode
- Uses `PackageManager.queryIntentActivities()` for LAUNCHER apps
- Build & run: open picker, select apps, return results

### Step 5: App Intentions — Data + UI
- `AppIntention` Room entity + `AppIntentionDao`
- `IntentionManager.kt` — create, update, delete, increment opens
- `IntentionConfigSheet.kt` — configure opens/time per open
- `IntentionRow.kt` + `IntentionCard.kt` — horizontal scroll on dashboard
- `DashboardViewModel.kt` — observe intentions from Room
- Build & run: create intentions, see them on dashboard

### Step 6: Shield Screen — Intention Variant
- `BlockedAppActivity.kt` + `ShieldScreen.kt`
- Intention shield variant: shows opens/streak, "Nevermind" / "Open [App]"
- Manifest configuration (excludeFromRecents, singleTask, taskAffinity)
- Navigation: back → home
- Build & run: manually launch BlockedAppActivity, verify UI and navigation

### Step 7: Monitoring Foreground Service
- `MonitoringService.kt` — foreground service with 1s UsageStats polling
- `BootCompletedReceiver.kt` — restart on boot
- Blocked list calculation (intentions not currently open)
- Detect foreground app → if blocked → launch BlockedAppActivity
- Service start/stop lifecycle tied to intention existence
- Build & run: create an intention, open the blocked app, see the shield

### Step 8: Intention Timed Open Window
- "Open [App]" on shield → unblock temporarily
- `IntentionAlarmReceiver.kt` — handles re-block alarm
- AlarmManager scheduling for re-block
- 30-second warning notification
- Re-shield on timer expiry (re-add to blocked list, launch shield if foreground)
- Build & run: open an app through the shield, use it for the time window, verify re-block

### Step 9: Blocking Sessions — State Machine + Shield
- `PresentSession` + `PresentSessionAction` Room entities + `PresentSessionDao`
- `SessionStateMachine.kt` — state transitions with validation
- `SessionManager.kt` — orchestrates start/stop/giveUp/complete
- `SessionConfigSheet.kt` — session setup bottom sheet
- Session shield variant on `BlockedAppActivity`
- Goal-reached shield variant
- Session priority over intentions in blocked list
- Build & run: configure and start a session, see session shield on blocked apps

### Step 10: Session Foreground Notification
- Persistent notification with chronometer (auto-updating timer)
- "End Session" action on notification
- Update notification on goal reached ("Goal Reached! +10 XP")
- Notification channels: `monitoring` (low priority), `session` (high priority)
- Build & run: start session, verify notification with timer on lock screen

### Step 11: Daily Reset + Streak Logic
- `DailyResetWorker.kt` — WorkManager periodic task at midnight
- Streak increment/break logic per intention
- Streak freeze grant on Mondays
- Streak freeze consumption on over-limit days
- Build & run: simulate midnight reset, verify streaks update correctly

### Step 12: Dashboard Assembly
- `ScreenTimeCard.kt` — circular progress + total time + per-app chips
- Wire up `DashboardViewModel` with all data sources:
  - Screen time from `UsageStatsRepository`
  - Intentions from Room
  - Active session state
  - XP/coins from DataStore
- Header bar with streak + XP
- Session CTA wired to `SessionConfigSheet`
- Permission health banner (if permissions missing)
- Build & run: complete dashboard with live data

---

## Appendix A: Android Permissions Manifest

```xml
<manifest>
    <!-- Core: screen time + foreground app detection -->
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />

    <!-- App picker: see all installed apps (Android 11+) -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission" />

    <!-- Notifications (runtime on Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Foreground service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <!-- Keep service alive -->
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

    <!-- Restart on boot -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Exact alarms for intention re-block + session goal -->
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

    <!-- Wake lock for alarm handling -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
</manifest>
```

## Appendix B: Known Limitations (MVP)

1. **~1 second flash** — blocked app is visible for up to 1s before shield covers it (UsageStats polling latency)
2. **Force-stop bypass** — user can force-stop BePresent to remove all blocking; no mitigation in MVP
3. **OEM battery kill** — Chinese OEM ROMs may kill the service; onboarding guides help but don't guarantee reliability
4. **No web domain blocking** — only apps can be blocked
5. **Soft enforcement** — user can always "Open Anyway" past their intention limit (streak will break)
6. **No scheduled sessions** — manual start only
7. **Boot gap** — 5–30 second window after reboot where blocking is inactive
8. **No widgets** — information only available in-app

## Appendix C: Files Referenced

- `planning/android-implementation-guide.md` — Android API code samples for all features
- `planning/android-critical-considerations.md` — Flash problem, OEM battery, bypass scenarios, testing checklist
- `planning/ios-features-reference.md` — Feature specs (intentions, sessions, shield states, XP table)
- `android-migration-plan/07-app-intentions-shield-and-android-equivalents.md` — Intention domain model + enforcement strategy
