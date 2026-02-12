# BePresent Android — Implementation Guide

How to build each iOS feature using Android APIs. Organized by feature area with specific APIs, permissions, limitations, and recommended approaches.

---

## 1. App Blocking / Shield Overlay

### iOS Approach
`ManagedSettings` → `ManagedSettingsStore.shield.applications` / `.applicationCategories` — OS-level enforcement, apps show a system shield when opened.

### Android Approach

Android has **no OS-level app blocking API** for third-party apps. You must detect foreground app changes and show a blocking overlay yourself.

**Two detection methods:**

#### Option A: UsageStatsManager Polling (Play Store safe)
```kotlin
val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
// Poll every ~1 second
val endTime = System.currentTimeMillis()
val beginTime = endTime - 1000
val events = usm.queryEvents(beginTime, endTime)
val event = UsageEvents.Event()
while (events.hasNextEvent()) {
    events.getNextEvent(event)
    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
        val packageName = event.packageName
        if (isBlocked(packageName)) showBlockingOverlay()
    }
}
```
- **Latency:** ~1 second (depends on poll interval)
- **Battery:** Medium (constant polling)
- **Play Store:** Safe — no special justification needed
- **Permission:** `PACKAGE_USAGE_STATS` (user grants in Settings)

#### Option B: AccessibilityService (instant detection, Play Store risky)
```kotlin
class AppMonitorService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (isBlocked(packageName)) showBlockingOverlay()
        }
    }
}
```
- **Latency:** ~100ms (event-driven, instant)
- **Battery:** Low (no polling)
- **Play Store:** Risky — requires Permissions Declaration Form justifying why UsageStats is insufficient. Google has removed apps for misusing this.
- **Config:** Must set `canRetrieveWindowContent="false"` to show Google you're not reading screen content

**Recommended:** Use UsageStats polling as default, offer AccessibilityService as optional "enhanced accuracy" mode.

### Showing the Block Screen

**Two approaches:**

#### Overlay (SYSTEM_ALERT_WINDOW)
```kotlin
val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
val params = WindowManager.LayoutParams(
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
    PixelFormat.TRANSLUCENT
)
// Add a ComposeView with your shield UI
windowManager.addView(shieldView, params)
```
- Can use Jetpack Compose for the overlay UI
- Must handle back button, touch interception
- User can dismiss by navigating home (not as tight as iOS)

#### Full-Screen Activity (preferred)
```kotlin
// Launch a blocking activity on top
val intent = Intent(context, BlockedAppActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    putExtra("blocked_package", packageName)
}
context.startActivity(intent)
```
- More reliable than overlay
- Can be a full Compose screen matching iOS shield design
- Set `excludeFromRecents="true"` and `launchMode="singleTask"`

### Key Limitations vs iOS
| iOS | Android |
|-----|---------|
| OS prevents app from launching | App launches briefly, then overlay covers it |
| Shield is part of the OS | Shield is your custom UI on top |
| User cannot bypass without ending session | Determined user can force-stop your service to bypass |
| Works even if your app is killed | Requires a running foreground service |
| Blocks web domains too | Can only block apps (no web domain blocking without VPN) |

### Beast Mode (Anti-Bypass)
On Android, a determined user can always force-stop your app. To make it harder:
- **Device Admin:** Register as device admin — user must deactivate admin before force-stopping
- **Device Owner (ADB setup):** Can truly lock apps using `DevicePolicyManager.setPackagesSuspended()` — this is OS-level blocking, but requires ADB command to set up
- **Accessibility Service:** If killed, can auto-restart via `onServiceConnected()`
- **Multiple services:** Use a watchdog pattern where services monitor each other

---

## 2. App Selection (Picker)

### iOS Approach
`FamilyActivityPicker` — system-provided UI that returns opaque `ApplicationToken`s. Apple intentionally hides package names for privacy.

### Android Approach
Build your own picker using `PackageManager`:

```kotlin
val pm = context.packageManager
val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
val apps = pm.queryIntentActivities(intent, 0).map { resolveInfo ->
    AppInfo(
        packageName = resolveInfo.activityInfo.packageName,
        label = resolveInfo.loadLabel(pm).toString(),
        icon = resolveInfo.loadIcon(pm),
        category = resolveInfo.activityInfo.applicationInfo.category // API 26+
    )
}
```

**App categories (API 26+):**
```kotlin
// ApplicationInfo.category values:
ApplicationInfo.CATEGORY_GAME        // Games
ApplicationInfo.CATEGORY_AUDIO       // Music, podcasts
ApplicationInfo.CATEGORY_VIDEO       // Video players
ApplicationInfo.CATEGORY_IMAGE       // Photo apps
ApplicationInfo.CATEGORY_SOCIAL      // Social media
ApplicationInfo.CATEGORY_NEWS        // News apps
ApplicationInfo.CATEGORY_MAPS        // Maps, navigation
ApplicationInfo.CATEGORY_PRODUCTIVITY // Office, tools
ApplicationInfo.CATEGORY_UNDEFINED   // Most apps fall here unfortunately
```

**Limitation:** Most apps return `CATEGORY_UNDEFINED`. You may need to maintain your own category mapping or use the Play Store category (requires a web scrape or API lookup).

**Advantage over iOS:** You get actual package names and app icons — no opaque tokens, full transparency.

**Permission:** `QUERY_ALL_PACKAGES` (required on Android 11+ to see all installed apps). Needs Play Store justification.

---

## 3. Scheduled Sessions

### iOS Approach
`DeviceActivitySchedule` with `DeviceActivityCenter.startMonitoring()` — OS calls your extension at the scheduled time even if app is killed.

### Android Approach

Use `AlarmManager` for exact timing + `BroadcastReceiver` to trigger:

```kotlin
// Schedule a session
val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
    putExtra("session_id", sessionId)
    putExtra("action", "start") // or "end"
}
val pendingIntent = PendingIntent.getBroadcast(
    context, requestCode, intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
alarmManager.setAlarmClock(
    AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent),
    pendingIntent
)
```

```kotlin
class SessionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra("session_id")
        val action = intent.getStringExtra("action")
        when (action) {
            "start" -> SessionManager.startScheduledSession(sessionId)
            "end" -> SessionManager.endScheduledSession(sessionId)
        }
    }
}
```

**Key considerations:**
- Use `setAlarmClock()` (not `setExact()`) — it survives Doze mode and shows in the system alarm UI
- **Permission:** `USE_EXACT_ALARM` or `SCHEDULE_EXACT_ALARM` (Android 12+)
- **Boot persistence:** Register a `BOOT_COMPLETED` receiver to re-schedule alarms after reboot
- **Recurring:** Re-schedule the next occurrence after each trigger (Android alarms are one-shot)

### Reliability
- `AlarmManager.setAlarmClock()` is the most reliable scheduling mechanism on Android
- Survives Doze mode, app standby, and battery optimization
- Will wake the device from sleep
- Works even if the app process is dead

---

## 4. App Intentions (Per-App Limits)

### iOS Approach
Custom `AppIntentionV2` model + `ManagedSettingsStore(named: .limits)` for per-app shielding + `DeviceActivityEvent` to monitor time-per-open.

### Android Approach

#### Tracking Opens
```kotlin
// Query UsageEvents for app open count today
fun getOpensToday(packageName: String): Int {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val events = usageStatsManager.queryEvents(calendar.timeInMillis, System.currentTimeMillis())
    var opens = 0
    val event = UsageEvents.Event()
    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.packageName == packageName &&
            event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            opens++
        }
    }
    return opens
}
```

#### Time-Per-Open Window
When user taps "Open [App]" on the shield:
1. Remove the app from blocked list temporarily
2. Start a `CountDownTimer` or schedule an `AlarmManager` for `timePerOpen` minutes
3. When timer fires, re-add the app to the blocked list and show a "closing" notification
4. Re-shield the app

```kotlin
fun openAppTemporarily(packageName: String, minutes: Int) {
    unblockedApps.add(packageName)

    // Schedule re-block
    handler.postDelayed({
        unblockedApps.remove(packageName)
        // Send "closing in 30s" notification
        handler.postDelayed({
            // Re-shield app
        }, 30_000)
    }, (minutes * 60 * 1000L) - 30_000)
}
```

#### Streak Tracking
Store in Room database:
```kotlin
@Entity
data class AppIntention(
    @PrimaryKey val id: String,
    val packageName: String,
    val allowedOpensPerDay: Int,
    val timePerOpenMinutes: Int,
    val totalOpensToday: Int = 0,
    val streak: Int = 0,
    val lastResetDate: String = "",
    val isFreezeActive: Boolean = false
)
```

Daily reset via WorkManager:
```kotlin
val dailyResetWork = PeriodicWorkRequestBuilder<DailyResetWorker>(
    1, TimeUnit.DAYS
).setInitialDelay(calculateMidnightDelay(), TimeUnit.MILLISECONDS)
 .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "daily_reset", ExistingPeriodicWorkPolicy.KEEP, dailyResetWork
)
```

---

## 5. Screen Time Tracking & Thresholds

### iOS Approach
`DeviceActivityMonitor` with threshold events every 30 minutes — the OS fires callbacks like `eventDidReachThreshold`.

### Android Approach

#### Reading Total Screen Time
```kotlin
fun getTotalScreenTimeToday(): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
    }
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        calendar.timeInMillis,
        System.currentTimeMillis()
    )
    return stats.sumOf { it.totalTimeInForeground } // milliseconds
}
```

#### Per-App Screen Time
```kotlin
fun getPerAppScreenTime(): Map<String, Long> {
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startOfDay, now
    )
    return stats.associate { it.packageName to it.totalTimeInForeground }
}
```

#### Threshold Monitoring
No event-driven API on Android. Must poll:

```kotlin
// In your foreground service, check every 5 minutes
class ThresholdMonitor(private val context: Context) {
    private val thresholdsMinutes = (1..32).map { it * 30 } // 30, 60, 90 ... 960
    private val reachedThresholds = mutableSetOf<Int>()

    fun check() {
        val totalMinutes = getTotalScreenTimeToday() / 60_000
        for (threshold in thresholdsMinutes) {
            if (totalMinutes >= threshold && threshold !in reachedThresholds) {
                reachedThresholds.add(threshold)
                onThresholdReached(threshold)
            }
        }
    }

    private fun onThresholdReached(minutes: Int) {
        // Send notification, update streak, sync with server
    }
}
```

### Score Calculation
Same formula as iOS — runs locally:
```kotlin
fun screenTimeScore(totalMinutes: Int): Int = maxOf(0, 100 - totalMinutes / 10)
fun screenTimePoints(totalHours: Double): Int = when {
    totalHours < 1 -> 100
    totalHours < 2 -> 75
    totalHours < 3 -> 50
    totalHours < 4 -> 35
    totalHours < 5 -> 20
    totalHours < 6 -> 10
    totalHours < 7 -> 5
    totalHours < 8 -> 3
    else -> 0
}
```

---

## 6. Live Activities → Persistent Notification

### iOS Approach
`ActivityKit` Live Activity on the lock screen and Dynamic Island — shows a timer counting up during a session.

### Android Approach

Use a **foreground service** with a rich notification:

```kotlin
class SessionForegroundService : Service() {
    private fun createNotification(session: Session): Notification {
        val chronometer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use Chronometer for auto-updating time
            RemoteViews(packageName, R.layout.notification_session).apply {
                setChronometer(R.id.timer, session.startTimeMillis, null, true)
            }
        } else null

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_present)
            .setContentTitle("Present Session Active")
            .setContentText("Be Present for ${session.goalMinutes}m")
            .setOngoing(true) // Can't be swiped away
            .setUsesChronometer(true) // Shows elapsed time
            .setChronometerCountDown(false)
            .setWhen(session.startTimeMillis)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows on lock screen
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .addAction(R.drawable.ic_stop, "End Session", endPendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(chronometer)
            .build()
    }
}
```

**Key features:**
- `setOngoing(true)` — cannot be dismissed (like iOS Live Activity persistence)
- `setUsesChronometer(true)` — auto-updating timer without any code
- `VISIBILITY_PUBLIC` — visible on lock screen
- Can update the notification when goal is reached (change text, add "Complete" action)
- Can include a progress bar: `.setProgress(goalMinutes, elapsedMinutes, false)`

**On goal reached, update notification:**
```kotlin
val completedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle("Session Goal Reached! 🎉")
    .setContentText("You earned +${session.xp} XP")
    .addAction(R.drawable.ic_check, "Complete", completePendingIntent)
    .addAction(R.drawable.ic_continue, "Stay Present", stayPendingIntent)
    .build()
notificationManager.notify(NOTIFICATION_ID, completedNotification)
```

---

## 7. Widgets

### iOS Approach
WidgetKit with `accessoryRectangular` (lock screen) and `systemSmall` (home screen).

### Android Approach

Use **Jetpack Glance** (Compose-based widgets):

#### Session Widget
```kotlin
class SessionWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val isActive = SessionManager.isActive()
            if (isActive) {
                ActiveSessionContent(
                    sessionName = SessionManager.currentSessionName(),
                    progress = SessionManager.progress()
                )
            } else {
                StartSessionContent()
            }
        }
    }
}

@Composable
fun ActiveSessionContent(sessionName: String, progress: Float) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
        Text("Session Active", style = TextStyle(fontSize = 12.sp))
        Text(sessionName, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
        LinearProgressIndicator(progress = progress)
    }
}
```

```xml
<!-- res/xml/session_widget_info.xml -->
<appwidget-provider
    android:minWidth="180dp"
    android:minHeight="40dp"
    android:updatePeriodMillis="600000"
    android:widgetCategory="home_screen|keyguard" />
```

#### Streak Widget
```kotlin
class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val streak = StreakManager.currentStreak()
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Image(ImageProvider(R.drawable.flame), "streak")
                Text("$streak", style = TextStyle(fontSize = 32.sp))
                Text("Day Streak 🔥", style = TextStyle(fontSize = 12.sp))
            }
        }
    }
}
```

**Updating widgets from your service:**
```kotlin
// Trigger widget update when session starts/ends
SessionWidget().update(context, GlanceAppWidgetManager(context).getGlanceIds(SessionWidget::class.java).first())
// Or broadcast-based:
val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
context.sendBroadcast(intent)
```

**Lock screen widgets:** Android 14+ supports lock screen widgets via `widgetCategory="keyguard"`.

---

## 8. Quick Actions (Long-Press Shortcuts)

### iOS Approach
`UIApplicationShortcutItem` — force-touch shortcuts on the app icon.

### Android Approach

```xml
<!-- res/xml/shortcuts.xml -->
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="unblock"
        android:enabled="true"
        android:icon="@drawable/ic_unlock"
        android:shortcutShortLabel="@string/unblock"
        android:shortcutLongLabel="@string/unblock_apps">
        <intent
            android:action="com.bepresent.ACTION_UNBLOCK"
            android:targetPackage="com.bepresent.android"
            android:targetClass="com.bepresent.android.MainActivity" />
    </shortcut>
    <shortcut
        android:shortcutId="feedback"
        android:enabled="true"
        android:icon="@drawable/ic_feedback"
        android:shortcutShortLabel="@string/feedback">
        <intent
            android:action="com.bepresent.ACTION_FEEDBACK"
            android:targetPackage="com.bepresent.android"
            android:targetClass="com.bepresent.android.MainActivity" />
    </shortcut>
</shortcuts>
```

Handle in `MainActivity`:
```kotlin
override fun onNewIntent(intent: Intent) {
    when (intent.action) {
        "com.bepresent.ACTION_UNBLOCK" -> {
            if (SessionManager.isActive()) showEndSessionDialog()
            else SessionManager.clearAllBlocking()
        }
        "com.bepresent.ACTION_FEEDBACK" -> navigateTo(Screen.Feedback)
    }
}
```

---

## 9. Background Monitoring & Persistence

### iOS Approach
`DeviceActivityMonitor` extension — OS-managed, runs independently of the main app, fires on events.

### Android Approach

Android requires a **foreground service** with a persistent notification:

```kotlin
class MonitoringForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMonitoringNotification())
        startPolling()
        return START_STICKY // Restart if killed
    }

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                // Check foreground app
                checkForegroundApp()
                // Check screen time thresholds
                thresholdMonitor.check()
                delay(1000) // 1-second interval
            }
        }
    }
}
```

**Keeping the service alive:**

1. **`START_STICKY`** — system restarts service if killed
2. **`BOOT_COMPLETED` receiver** — restart on device reboot
3. **Battery optimization exemption** — request `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
4. **Foreground notification** — required for foreground services, keeps priority high

**OEM battery killers (biggest Android challenge):**
Chinese manufacturers (Xiaomi, Huawei, Oppo, Vivo, OnePlus) aggressively kill background services. Mitigations:
- Guide users to whitelist your app in battery settings
- Use [Don't Kill My App](https://dontkillmyapp.com/) guides per manufacturer
- Show an in-app setup flow for each OEM

```kotlin
// Detect manufacturer and show appropriate guide
fun getOemBatteryGuide(): String? = when (Build.MANUFACTURER.lowercase()) {
    "xiaomi", "redmi" -> "Settings > Apps > Manage Apps > BePresent > Battery Saver > No restrictions"
    "huawei", "honor" -> "Settings > Apps > BePresent > Battery > Launch: Manual, all toggles ON"
    "samsung" -> "Settings > Battery > Background usage limits > Never sleeping apps > Add BePresent"
    "oppo", "realme" -> "Settings > Battery > Power saving > BePresent > Allow background activity"
    "oneplus" -> "Settings > Battery > Battery optimization > BePresent > Don't optimize"
    else -> null
}
```

---

## 10. Notifications

### iOS Approach
`UNUserNotificationCenter` with calendar triggers, time interval triggers, and from the monitor extension.

### Android Approach

```kotlin
// Create notification channel (required Android 8+)
val channel = NotificationChannel(
    "session_channel",
    "Session Notifications",
    NotificationManager.IMPORTANCE_HIGH
).apply { description = "Notifications about your present sessions" }
notificationManager.createNotificationChannel(channel)

// Immediate notification
fun showNotification(title: String, body: String, id: Int) {
    val notification = NotificationCompat.Builder(context, "session_channel")
        .setSmallIcon(R.drawable.ic_present)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    notificationManager.notify(id, notification)
}

// Scheduled notification via AlarmManager
fun scheduleNotification(title: String, body: String, triggerAtMillis: Long, requestCode: Int) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("body", body)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setAlarmClock(
        AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
        pendingIntent
    )
}
```

**Permission:** `POST_NOTIFICATIONS` (Android 13+ requires runtime permission request)

---

## 11. Permissions & Authorization

### iOS Approach
Single `FamilyControls` authorization request.

### Android Approach
Multiple separate permissions, each requiring different grant flows:

| Permission | How to Request | What It Enables |
|-----------|---------------|-----------------|
| `PACKAGE_USAGE_STATS` | `Settings.ACTION_USAGE_ACCESS_SETTINGS` — user toggles in Settings | Screen time data, foreground app detection |
| `SYSTEM_ALERT_WINDOW` | `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` — user toggles in Settings | Overlay blocking screen |
| `POST_NOTIFICATIONS` | Runtime permission dialog (Android 13+) | Notifications |
| `USE_EXACT_ALARM` | Automatic on Android 13+, or `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` on 12 | Scheduled sessions |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep service alive |
| `BIND_ACCESSIBILITY_SERVICE` | User enables in Settings > Accessibility | Instant app detection (optional) |
| `QUERY_ALL_PACKAGES` | Automatic (but needs Play Store justification) | See all installed apps |
| `RECEIVE_BOOT_COMPLETED` | Manifest-only | Restart after reboot |
| `FOREGROUND_SERVICE` | Manifest-only | Run foreground service |

**Onboarding flow should request these step by step:**
1. Usage Access (essential)
2. Overlay permission (essential)
3. Notifications (important)
4. Battery optimization exemption (important)
5. Accessibility Service (optional, "enhanced mode")
6. Exact alarms (if scheduling)

---

## 12. Accessibility Service — Deep Dive

### What It Does
`AccessibilityService` receives callbacks when any window changes, giving you instant foreground app detection without polling.

### Configuration
```xml
<!-- res/xml/accessibility_service_config.xml -->
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:canRequestFilterKeyEvents="false"
    android:description="@string/accessibility_description" />
```

### Play Store Policy (Critical)
Google has cracked down on AccessibilityService misuse:

- Apps requesting `BIND_ACCESSIBILITY_SERVICE` trigger **manual review**
- You must submit a **Permissions Declaration Form** explaining why you need it
- Must demonstrate there is **no alternative API**
- Approved non-accessibility uses include: screen time / parental control apps (when accessibility is the only way)
- Required justification: "Our app uses AccessibilityService to detect foreground app changes for screen time management. This functionality cannot be achieved with any other Android API due to the limitations of UsageStatsManager polling (battery drain, latency). We do not use AccessibilityService to read screen content, interact with UI elements, or collect user data."
- Review can take weeks and may result in rejection

### Recommendation
- Use UsageStats polling as the **default** and offer AccessibilityService as optional "enhanced accuracy" mode
- Have a complete fallback if Accessibility is not enabled
- Consider sideloading / direct APK for users who want full experience without Play Store restrictions

---

## 13. Device Policy Manager / Device Owner

### What It Can Do
`DevicePolicyManager` can **truly block apps at the OS level**:

```kotlin
val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
// Suspend packages — hides apps, prevents launch
dpm.setPackagesSuspended(admin, arrayOf("com.instagram.android"), true)
// Or completely hide from launcher
dpm.setApplicationHidden(admin, "com.instagram.android", true)
```

### The Catch
Device Owner can **only** be set:
1. During factory reset (NFC/QR provisioning)
2. Via ADB command: `adb shell dpm set-device-owner com.bepresent.android/.MyDeviceAdminReceiver`
3. Via enterprise MDM

### Is This Viable for BePresent?
**Not for average consumers.** But viable for:
- Power users willing to run an ADB command (some screen time apps like "Lock Me Out" do this)
- "Unbreakable Beast Mode" — an opt-in feature with a setup guide
- Parental control use cases

---

## 14. Digital Wellbeing APIs

### Does Google Offer Official APIs?

**No.** Unlike Apple's comprehensive Screen Time framework, Google has NOT released public APIs for third-party screen time apps.

What Google has (all closed to third parties):
- **Digital Wellbeing app** — built into Android, shows screen time, app timers, focus mode. No API.
- **Family Link** — parental controls with app limits. No API.
- **WellbeingExtras (Android 10+)** — undocumented internal APIs. Not available.
- **Focus Mode** — part of Digital Wellbeing. Not available.

**What this means:** You must cobble together multiple APIs:
- `UsageStatsManager` for reading screen time data
- `AccessibilityService` or `UsageStatsManager` polling for detecting foreground apps
- `SYSTEM_ALERT_WINDOW` for overlay blocking
- `AlarmManager` for scheduling
- `ForegroundService` for persistence
- Build everything else yourself

---

## 15. Web Domain Blocking

### iOS Approach
`ManagedSettings.shield.webDomains` — blocks websites system-wide in Safari and other browsers.

### Android Approach
No system API for this. Options:
- **Local VPN:** Create a `VpnService` that intercepts DNS queries and blocks specific domains. Apps like "Blokada" use this approach. Most reliable.
- **Accessibility Service:** Detect browser URLs from the accessibility event tree (if `canRetrieveWindowContent="true"` — but this makes Play Store approval harder)
- **DNS-over-HTTPS:** Set a custom DNS resolver that blocks domains (Android 9+ Private DNS)

**Recommendation:** Skip web domain blocking for V1. Focus on app blocking. Add VPN-based blocking later as a separate feature if needed.

---

## 16. Play Store Policy Summary

| Permission | Risk Level | Justification Needed |
|-----------|-----------|---------------------|
| `PACKAGE_USAGE_STATS` | Low | "Track screen time and app usage for digital wellbeing" |
| `SYSTEM_ALERT_WINDOW` | Medium | "Display app blocking screen during active sessions" |
| `BIND_ACCESSIBILITY_SERVICE` | High | Detailed Permissions Declaration Form |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Low | "Persistent monitoring of screen time" |
| `USE_EXACT_ALARM` | Low | "Schedule blocking sessions at exact times" |
| `QUERY_ALL_PACKAGES` | Medium | "Display installed apps for user to select which to block" |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Low | Valid for screen time monitoring |

### Alternative Distribution
If Play Store policies are too restrictive:
- **Direct APK download** from your website
- **Samsung Galaxy Store** — different policies
- **F-Droid** — no restrictions (open source only)

---

## 17. iOS-to-Android Feature Parity Summary

| iOS Feature | iOS API | Android API | Parity Level |
|-------------|---------|-------------|-------------|
| App blocking (shield) | ManagedSettingsStore.shield | Overlay + UsageStats/Accessibility | Partial — user can bypass |
| Shield UI | ShieldConfigurationExtension | BlockedAppActivity / Overlay | Full — custom UI is more flexible |
| App picker | FamilyActivityPicker | Custom PackageManager picker | Full — more flexible, shows real names |
| Scheduled sessions | DeviceActivitySchedule | AlarmManager.setAlarmClock | Full |
| Screen time tracking | DeviceActivityMonitor thresholds | UsageStatsManager polling | Partial — polling, not event-driven |
| Per-app limits | DeviceActivityEvent per-app | UsageEvents + timers | Full |
| Live Activity | ActivityKit | Foreground service notification | Full — more flexible |
| Lock screen widget | WidgetKit lock screen | Lock screen notification | Different mechanism, same effect |
| Home screen widget | WidgetKit | Jetpack Glance | Full |
| Quick actions | UIApplicationShortcutItem | ShortcutManager | Full |
| Background monitoring | DeviceActivityMonitor extension | Foreground service | Full — but requires visible notification |
| Notifications | UNUserNotificationCenter | NotificationManager + AlarmManager | Full |
| Beast Mode | UI-only (OS enforces blocking) | Device Admin / Device Owner | Partial — requires extra setup |
| Web domain blocking | ManagedSettings.shield.webDomains | VPN service (complex) | Partial — skip for V1 |
| Authorization | FamilyControls (single prompt) | 5+ separate permissions | More complex setup |

---

## 18. Recommended Build Order

### Phase 1: Core Infrastructure
- Kotlin + Jetpack Compose project setup with Hilt DI
- Permission manager + onboarding flow (UsageStats, Overlay, Notifications)
- UsageStatsManager wrapper for screen time queries
- App picker using PackageManager
- Room database + DataStore for preferences

### Phase 2: Session Blocking
- Session manager (create, start, end, state machine)
- Foreground service with persistent notification + timer
- UsageStats polling for foreground app detection (1s interval)
- BlockedAppActivity with shield UI
- XP/coins calculation on completion

### Phase 3: Scheduling & Intentions
- AlarmManager scheduled sessions + boot receiver
- App intentions (per-app limits, opens tracking, time-per-open)
- Streak tracking with daily reset via WorkManager
- Streak freeze logic

### Phase 4: Enhanced Features
- AccessibilityService option for instant detection
- Screen time threshold monitoring + witty notifications
- Widgets (Glance) — session progress + streak
- Quick actions (shortcuts.xml)
- Server sync + analytics

### Phase 5: Polish
- OEM-specific battery optimization setup guides
- Device Admin option for Beast Mode power users
- Play Store compliance (Permissions Declaration Forms)
- Widget + notification polish
