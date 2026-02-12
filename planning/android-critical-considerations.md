# Android Screen Time Implementation — Critical Considerations

This doc covers the hard truths about building a screen time / app blocking app on Android. Read this before writing any code.

---

## 1. "Softer Blocking" — What This Actually Means

### On iOS
When you call `ManagedSettingsStore.shield.applications = [tokens]`, the **operating system itself** enforces the block. Your app can be killed, deleted from memory, even crash — the shield persists because it's enforced by a system daemon, not your app process. The shield is rendered by a separate OS-managed extension process. The user literally cannot open the blocked app. The only way to remove the shield is to call `clearAllSettings()` from your code, or revoke Screen Time authorization entirely (which requires going to Settings > Screen Time > and removing your app — a multi-step intentional process).

### On Android
There is no system-level app blocking API. Your app must:
1. **Detect** that a blocked app moved to the foreground (via polling or AccessibilityService)
2. **React** by launching your blocking screen on top of it

This means **your app process must be alive and running** for blocking to work. If your process dies, there is a gap where the user can use blocked apps freely until your service restarts.

### What Happens When Your App Is Backgrounded
Backgrounding is fine. Your **foreground service** runs independently of your UI. The user can close your app's UI (swipe it away from recents) and the foreground service keeps running, keeps polling, keeps blocking. This is the normal operating state — your UI is closed, your service is active.

### What Happens When Your App Is Force-Stopped
This is the real problem. If the user goes to **Settings > Apps > BePresent > Force Stop**, your entire process dies — foreground service, accessibility service, everything. Blocking stops immediately. There is no way to prevent this on a non-rooted phone.

**Mitigations:**
- **Device Admin:** If registered as a device admin, the user must first deactivate admin in Settings before they can force-stop. This adds friction (2 extra steps).
- **Device Owner (ADB):** If set as device owner, you can use `setPackagesSuspended()` which persists even after your process dies because it's enforced by the OS. This is true OS-level blocking — but requires the user to run an ADB command during setup.
- **Accessibility Service:** Android's accessibility framework automatically restarts your service if killed (unless force-stopped). This provides better resilience than a plain foreground service.

### What Happens When Your Process Is Killed by the OS
Android can kill your process for memory pressure. This is different from force-stop:
- `START_STICKY` on your service means the OS will restart it, usually within a few seconds
- But there IS a gap (typically 1-10 seconds, sometimes longer on OEM ROMs) where blocking is inactive
- `AccessibilityService` restarts faster and more reliably than a regular foreground service
- **OEM battery optimization** is the biggest threat here (see Section 4)

### What Happens When the Phone Reboots
Your service does not auto-start. You need a `BOOT_COMPLETED` BroadcastReceiver that restarts your foreground service on boot. This works, but there's a ~5-30 second window after boot before your receiver fires and your service starts.

```
Boot sequence:
  Phone boots → system ready → BOOT_COMPLETED broadcast → your receiver → start service → resume blocking
  |_____________ ~5-30 seconds of no blocking _____________|
```

### Summary: Blocking Durability

| Scenario | iOS | Android (Foreground Service) | Android (Accessibility) | Android (Device Owner) |
|----------|-----|-----|-----|-----|
| App UI closed | Blocks | Blocks | Blocks | Blocks |
| App process killed by OS | Blocks | Gap (1-10s), then resumes | Gap (1-5s), then resumes | Blocks (OS-enforced) |
| User force-stops app | Blocks | **Blocking stops** | **Blocking stops** | Blocks (OS-enforced) |
| Phone reboots | Blocks | Gap until boot receiver fires | Gap until boot receiver fires | Blocks (OS-enforced) |
| App uninstalled | Blocks until Screen Time auth revoked | **Blocking stops** | **Blocking stops** | N/A (can prevent uninstall) |
| OEM battery kill | N/A | **Blocking stops** (may not restart) | **Blocking stops** (may not restart) | Blocks |

---

## 2. Scheduled Sessions — Does the App Need to Be Active?

### Short answer: No, but your service needs to restart.

`AlarmManager.setAlarmClock()` is the most reliable scheduling mechanism on Android. It:
- Fires even if the app process is dead
- Wakes the device from sleep / Doze mode
- Survives app standby
- Is treated as a user-visible alarm (like an alarm clock) so the OS gives it high priority

When the alarm fires, your `BroadcastReceiver.onReceive()` is called. From there you start your foreground service + begin blocking. The sequence:

```
Alarm fires → BroadcastReceiver.onReceive() → startForegroundService() → service starts → blocking begins
|_____________ ~1-3 seconds _____________|
```

### Caveats
1. **The alarm WILL fire.** `setAlarmClock()` is not affected by Doze or battery optimization. But your receiver has ~10 seconds to do its work before the system kills the broadcast context, so you must immediately delegate to a service.

2. **OEM battery killers can break this.** On Xiaomi/Huawei/Oppo, if the user hasn't whitelisted your app, the alarm may not fire or the service may be killed immediately after starting. This is the single biggest reliability problem on Android.

3. **Boot persistence.** Alarms are cleared on reboot. Your `BOOT_COMPLETED` receiver must re-schedule all pending alarms.

4. **Recurring scheduling.** Unlike iOS `DeviceActivitySchedule` which handles repeating schedules natively, Android alarms are one-shot. After each alarm fires, you must schedule the next occurrence:
   ```
   Session starts (Monday 9am alarm fires) → immediately schedule Monday 5pm end alarm
   Session ends (Monday 5pm alarm fires) → immediately schedule Tuesday 9am start alarm
   ```

---

## 3. How the Blocked/Shield Screen Works on Android

### The Core Problem
On iOS, the shield is a system-rendered overlay that the app literally cannot dismiss or draw over. On Android, you're showing your own UI on top of another app. The blocked app IS running underneath — you're just covering it.

### Two Implementation Approaches

#### Approach A: Full-Screen Activity (Recommended)

When you detect a blocked app in the foreground, launch a full-screen Activity:

```kotlin
// In your monitoring service, when blocked app detected:
val intent = Intent(context, BlockedAppActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
    putExtra("blocked_package", detectedPackage)
    putExtra("shield_type", "session") // or "intention"
}
context.startActivity(intent)
```

The `BlockedAppActivity` is a full-screen Compose screen that matches your iOS shield design. It shows:
- Session shield: session name, "Be Present" button, "Unlock?" button
- Intention shield: opens count, streak, "Nevermind" / "Open [App]" buttons

**What the user experiences:** They tap Instagram → Instagram starts to open → within ~300ms-1s your BlockedAppActivity launches on top → they see the shield. The blocked app may flash briefly before the shield covers it.

**Handling the back button and navigation:**
```kotlin
// In BlockedAppActivity
override fun onBackPressed() {
    // Don't call super — prevent going back to the blocked app
    // Instead, go home
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(homeIntent)
}

// Prevent task switcher from revealing the blocked app
override fun onPause() {
    super.onPause()
    // If the blocked app is still in foreground when we pause,
    // the monitoring service will re-launch this activity
}
```

**Manifest configuration:**
```xml
<activity
    android:name=".features.blocking.BlockedAppActivity"
    android:exported="false"
    android:excludeFromRecents="true"
    android:launchMode="singleTask"
    android:taskAffinity=""
    android:theme="@style/Theme.FullScreenBlocking" />
```

#### Approach B: System Overlay (SYSTEM_ALERT_WINDOW)

Draw a view directly on top of everything using WindowManager:

```kotlin
val params = WindowManager.LayoutParams(
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
    PixelFormat.OPAQUE
)
params.gravity = Gravity.CENTER
windowManager.addView(shieldComposeView, params)
```

**Pros over Activity approach:**
- No brief flash of the blocked app (overlay is already on screen)
- Can be shown instantly from your service (no Activity launch delay)

**Cons:**
- Harder to handle touch events and navigation
- `FLAG_NOT_FOCUSABLE` means the user can still interact with the app behind it (bad). But `FLAG_NOT_TOUCH_MODAL` blocks touch but also blocks the home button on some devices.
- Some Android versions / OEMs restrict overlays on the lock screen
- More complex lifecycle management (you must manually add/remove the view)

**Recommendation: Use the Activity approach** for the main shield. It's more reliable, handles navigation properly, and is easier to build with Compose. Use overlays only if you need instant coverage with zero flash.

### The "Flash" Problem

With the Activity approach, there's a brief moment where the user sees the blocked app before your shield covers it:

```
User taps Instagram → Instagram opens (visible for 200-800ms) → your service detects it → launches BlockedAppActivity → shield appears
```

**With UsageStats polling (1s interval):** Flash can be up to 1 second. The blocked app is visually open for up to 1 second.

**With AccessibilityService:** Flash is ~100-300ms. Much faster, but not instant.

**Mitigations:**
- Accept the flash — most Android screen time apps have this. Users understand.
- Use AccessibilityService for faster detection
- Pre-warm the BlockedAppActivity so it launches faster
- Consider a hybrid: overlay for instant coverage, then transition to Activity for interaction

### What the User Can Do to Bypass (and What You Can't Prevent)

| Bypass | Can You Prevent It? |
|--------|-------------------|
| Use app during the 200ms-1s flash | No (unless using overlay approach) |
| Navigate back to the blocked app from recents | Yes — re-detect and re-launch shield |
| Split-screen the blocked app | Partially — detect and cover, but tricky |
| Force-stop BePresent | No (unless Device Admin/Owner) |
| Uninstall BePresent | No (unless Device Owner) |
| Disable overlay permission in Settings | No — blocking will silently stop |
| Disable Usage Stats permission in Settings | No — detection will silently stop |
| Disable Accessibility Service | No — falls back to polling |

---

## 4. OEM Battery Optimization — The #1 Android Problem

This is the single biggest difference from iOS and the hardest problem you'll face. Chinese Android manufacturers (which make up the majority of Android phones globally) aggressively kill background processes to save battery.

### The Problem
On stock Android (Pixel), a foreground service with a visible notification is virtually unkillable. On Xiaomi, Huawei, Oppo, Vivo, OnePlus, and Samsung, the OS **will kill your service** unless the user has specifically whitelisted your app.

### Impact on BePresent
If your service is killed:
- **Active session:** blocking stops. The user's apps are unblocked without them doing anything.
- **Screen time tracking:** threshold monitoring stops.
- **App intentions:** opens stop being tracked, re-shielding after time-per-open expires won't happen.
- **Scheduled sessions:** the alarm fires but the service may be killed immediately after starting.

### What You Must Do

**1. Onboarding must include OEM-specific battery setup:**
Show step-by-step instructions based on `Build.MANUFACTURER`:

| OEM | Required Setting |
|-----|-----------------|
| **Xiaomi/Redmi** | Settings > Apps > Manage Apps > BePresent > Battery Saver > "No restrictions". Also: Settings > Battery > Battery Saver > turn off or whitelist |
| **Huawei/Honor** | Settings > Apps > BePresent > Battery > App Launch > Manual (enable all 3 toggles). Also: Settings > Battery > Power Consumption Details > Lock BePresent |
| **Samsung** | Settings > Battery > Background usage limits > Never sleeping apps > add BePresent. Also: Settings > Apps > BePresent > Battery > Unrestricted |
| **Oppo/Realme** | Settings > Battery > App Quick Freeze > disable for BePresent. Also: ColorOS Task Manager > lock BePresent |
| **OnePlus** | Settings > Battery > Battery Optimization > BePresent > Don't optimize |
| **Vivo** | Settings > Battery > Background Power Consumption Management > BePresent > Off |

**2. Detect if your service was killed unexpectedly:**
When your service starts, check if there was an active session that shouldn't have ended. If so, immediately resume blocking and show a notification: "Your session was briefly interrupted but is now active again."

**3. Use multiple persistence mechanisms:**
- Foreground service with `START_STICKY`
- `AlarmManager` watchdog that checks every 15 minutes if your service is running
- `WorkManager` periodic task as a backup check
- `AccessibilityService` (hardest for OEMs to kill — it's a system-managed service)

**4. Test on real OEM devices:**
The emulator and Pixel phones will NOT reproduce these issues. You MUST test on Xiaomi, Samsung, and Huawei to catch battery kill behavior.

---

## 5. App Intentions on Android — How the Full Flow Works

This is the most complex feature because it combines detection, temporary unblocking, timed windows, and re-blocking.

### Data Model
```kotlin
data class AppIntention(
    val id: String,
    val packageName: String,       // e.g. "com.instagram.android"
    val allowedOpensPerDay: Int,   // e.g. 3
    val timePerOpenMinutes: Int,   // e.g. 5
    var totalOpensToday: Int,      // incremented each open
    var streak: Int,               // days within limit
    var lastResetDate: LocalDate,
    var currentlyOpen: Boolean,    // is the timed window active?
    var openedAt: Long?,           // timestamp of current open
)
```

### The Flow

```
[App is shielded — in your blocked list]
        │
User taps the app → your service detects it → launches BlockedAppActivity
        │
BlockedAppActivity shows intention shield:
  - "Open Instagram?"
  - "2/3 Opens Today"
  - "14 Day Streak 🔥"
  - [Nevermind]  [Open Instagram]
        │
User taps "Nevermind" → go home, app stays blocked
        │
User taps "Open Instagram" →
        │
    ┌───┴───────────────────────────────────────────────┐
    │  1. Increment totalOpensToday                      │
    │  2. Remove package from blocked list temporarily   │
    │  3. Set currentlyOpen = true, openedAt = now       │
    │  4. Schedule re-block alarm for timePerOpen mins   │
    │  5. Send notification: "Opening for 5 minutes"     │
    │  6. Finish BlockedAppActivity (reveals the app)    │
    └───────────────────────────────────────────────────┘
        │
    User uses the app freely for timePerOpen minutes
        │
    30 seconds before time expires →
        Send notification: "Closing Instagram in 30 seconds"
        │
    Time expires → alarm fires →
        │
    ┌───┴───────────────────────────────────────────────┐
    │  1. Add package back to blocked list               │
    │  2. Set currentlyOpen = false                      │
    │  3. If app is currently in foreground:              │
    │     → launch BlockedAppActivity on top             │
    │  4. Send notification: "Instagram closed"          │
    └───────────────────────────────────────────────────┘
        │
    At midnight → daily reset:
        │
    ┌───┴───────────────────────────────────────────────┐
    │  If totalOpensToday <= allowedOpensPerDay:         │
    │    → streak++                                      │
    │  Else:                                             │
    │    → streak = 0 (broken)                           │
    │    → notify accountability partners                │
    │  Reset totalOpensToday = 0                         │
    └───────────────────────────────────────────────────┘
```

### The Tricky Parts

**1. Re-blocking after time expires:**
The alarm fires via `AlarmManager`, but if your service was killed, re-blocking doesn't happen. The app stays unblocked indefinitely. Mitigate with:
- `AlarmManager.setAlarmClock()` for the re-block (highest priority alarm)
- A watchdog in your service that checks: "is any intention's time window overdue?" and re-blocks
- Store `openedAt` timestamp so you can recalculate on service restart

**2. Detecting if the timed app is still in foreground when re-blocking:**
When the re-block alarm fires, check if the app is currently in foreground. If yes, immediately launch BlockedAppActivity. If no, just add it back to the blocked list silently.

**3. Multiple intentions active simultaneously:**
User might have intentions on Instagram, TikTok, and Twitter. Each has its own timer and state. Your blocked list is the union of all intention packages minus currently-open ones.

**4. Interaction with sessions:**
If both a session AND an intention apply to an app:
- Session takes priority (same as iOS)
- If the app is in the session's allow list but has an intention → show intention shield
- If the app is blocked by the session → show session shield, regardless of intention state

**5. Streak freeze:**
When freeze is active, opens don't count toward breaking the streak. The shield still shows (with "Streak Freeze Active" variant) but the opens are tracked as "free."

---

## 6. Detection Latency — The User Experience Gap

| Method | Detection Time | User Sees Blocked App For |
|--------|---------------|--------------------------|
| AccessibilityService | ~100ms | Brief flash, barely noticeable |
| UsageStats polling @ 500ms | ~500ms | Half second, noticeable |
| UsageStats polling @ 1s | ~1s | Full second, clearly visible |
| UsageStats polling @ 2s | ~2s | Uncomfortably long |

On iOS, the user NEVER sees the blocked app. The shield replaces the app icon tap entirely.

On Android, the user ALWAYS sees the blocked app briefly. The question is how long.

**Recommendation:** Start with 1s polling (battery-safe, Play Store safe). Offer AccessibilityService as "Enhanced Blocking" in settings for users who want faster detection. In your marketing and UX, don't promise "instant" blocking — frame it as "we'll remind you and redirect you."

---

## 7. Permissions — What Breaks If Revoked

Users can revoke permissions at any time in Android Settings. Your app must handle graceful degradation:

| Permission Revoked | Impact | Detection | Recovery |
|-------------------|--------|-----------|----------|
| Usage Stats Access | Cannot detect foreground app OR read screen time | Check `AppOpsManager` on service start | Show "BePresent needs permission to work" notification, deep-link to Settings |
| Overlay Permission | Cannot show blocking Activity/overlay from service context | Check `Settings.canDrawOverlays()` | Same — notification + deep-link |
| Notification Permission | No notifications, foreground service still works | Check `NotificationManagerCompat.areNotificationsEnabled()` | Prompt re-enable |
| Battery Optimization | Service may be killed | Check `PowerManager.isIgnoringBatteryOptimizations()` | Prompt re-enable |
| Accessibility Service | Falls back to polling (slower detection) | Check `AccessibilityManager` | Notification suggesting re-enable |

**You must check permissions on every service start** and degrade gracefully rather than crashing. Show persistent UI if critical permissions are missing.

---

## 8. Android Version Fragmentation

| Feature | Minimum API | Notes |
|---------|-------------|-------|
| UsageStatsManager | API 21 (Android 5) | Core feature, widely available |
| SYSTEM_ALERT_WINDOW auto-grant | API 23 (Android 6) | Below 23 it's auto-granted; 23+ requires Settings toggle |
| Notification channels | API 26 (Android 8) | Required for notifications |
| Foreground service type | API 29 (Android 10) | Must declare `foregroundServiceType` |
| QUERY_ALL_PACKAGES restriction | API 30 (Android 11) | Need the permission to list installed apps |
| Exact alarm permission | API 31 (Android 12) | `SCHEDULE_EXACT_ALARM` needs user grant; `USE_EXACT_ALARM` auto-granted for alarm apps |
| POST_NOTIFICATIONS runtime permission | API 33 (Android 13) | Must request at runtime |
| Foreground service type `specialUse` | API 34 (Android 14) | New type for screen time use case |
| Lock screen widgets | API 34 (Android 14) | `widgetCategory="keyguard"` |
| Background activity launch restrictions | API 29+ (tightened each version) | Cannot launch Activities from background on Android 10+ without a foreground service or full-screen intent |

**Recommended minimum: API 26 (Android 8).** This covers ~95% of active devices and avoids the worst fragmentation.

**Critical: Background Activity launch restrictions (Android 10+):**
Starting an Activity from a background service is restricted. You MUST have an active foreground service with a visible notification for `startActivity()` to work from your service. Without this, your blocking Activity will silently fail to launch. This is the most common "it works on the emulator but not on a real device" bug.

---

## 9. Testing Checklist

Before shipping, verify these scenarios on real devices:

- [ ] Block works with app UI closed (swiped from recents)
- [ ] Block resumes after process killed by OS (wait for `START_STICKY` restart)
- [ ] Block resumes after phone reboot (BOOT_COMPLETED receiver)
- [ ] Scheduled session starts while app is not running
- [ ] Scheduled session starts while phone is in Doze mode
- [ ] App intention timed window expires while app is backgrounded
- [ ] App intention timed window expires while phone is locked
- [ ] Multiple intentions with overlapping timers
- [ ] Session + intention on same app (session takes priority)
- [ ] Permission revoked mid-session (graceful degradation)
- [ ] Battery optimization kills service on Xiaomi (test with real device)
- [ ] Battery optimization kills service on Samsung (test with real device)
- [ ] Split-screen with blocked app
- [ ] Picture-in-picture with blocked app
- [ ] Rapid app switching between blocked apps
- [ ] 100+ apps in blocked list (performance)
- [ ] Widget updates during active session
- [ ] Notification shown on lock screen during session
