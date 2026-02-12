# BePresent iOS — Feature Reference for Android Port

This document captures every user-facing feature of the iOS BePresent app that relies on Apple's Screen Time APIs (FamilyControls, ManagedSettings, DeviceActivity) and related frameworks. Use it as the source-of-truth spec when building the Android equivalent.

---

## 1. Present Sessions (Core Loop)

A **Present Session** is a timed commitment to stay off your phone. The user configures it and hits "Start"; their chosen apps are blocked until the session ends.

### Configuration Options
| Setting | Details |
|---|---|
| **Duration** | 5 min – 2 hours (manual), 20 min – 12 hours (scheduled) |
| **Mode** | *Allow List* (block everything except chosen apps) or *Block List* (block only chosen apps) |
| **Beast Mode** | Pro-only toggle. If on, the user **cannot** give up — the "Give Up" button is disabled for the session |
| **Session name & emoji** | Cosmetic personalization |

### Session Lifecycle
1. **Create** — save config (name, emoji, goal, mode, beast mode)
2. **Start** — apply shields to apps, start timer, launch Live Activity, schedule "goal reached" notification
3. **During** — apps show a shield overlay when opened. User can "Give Up" (unless beast mode) or keep going
4. **Goal Reached** — shield changes to a celebration state ("Session Goal Reached!"). User can end or keep going ("extra time")
5. **End** — remove all shields, award XP & coins, record to stats, end Live Activity

### End States
- **Completed** — goal duration met (or exceeded)
- **Gave Up** — user quit before goal
- **Canceled** — user quit within first 10 seconds (no penalty)

---

## 2. App Blocking & Shield Overlay

When a session is active and the user opens a blocked app, a **full-screen shield** appears instead of the app.

### Shield States
| State | Title | Primary Button | Secondary Button |
|---|---|---|---|
| **Active session** | Session name | "Be Present" (dismiss) | "Unlock?" (shows instructions) |
| **Goal reached** | "Session Goal Reached!" + XP | "Complete" (opens app) | "Stay Present" |
| **Tutorial** | "App is Blocked" | "Open BePresent" | — |
| **App Intention** | "Open [App]?" + opens/streak | "Nevermind" (dismiss) | "Open [App]" (starts timed window) |
| **Streak Freeze** | "Streak Freeze Active" | "Nevermind" | "Open [App]" |

### How Blocking Works by Mode
- **Allow List**: Shield ALL app categories, **except** apps the user whitelisted
- **Block List**: Shield **only** the specific apps, categories, and web domains the user selected

---

## 3. App Selection (Picker)

Users choose which apps to block/allow via a system-provided **FamilyActivityPicker**. This picker surfaces:
- Individual apps (as opaque `ApplicationToken`s)
- App categories (as `ActivityCategoryToken`s)
- Web domains (as `WebDomainToken`s)

Selections are saved to shared UserDefaults so all extensions (widget, monitor, shield) can read them.

---

## 4. Scheduled Sessions

Users can create **recurring sessions** that auto-start and auto-stop.

- **Config**: start time, end time, days of the week
- **Behavior**: A background monitor fires at the scheduled start time → automatically creates & starts a session → automatically ends it at the stop time
- **Limits**: Free users get 1 scheduled session; Pro users get unlimited
- **Minimum duration**: 20 minutes

---

## 5. App Intentions (Per-App Limits)

A separate feature from sessions. Users set **daily open limits** on specific apps.

### Config per App
| Setting | Details |
|---|---|
| **Allowed opens per day** | e.g., 3 opens |
| **Time per open** | e.g., 5 minutes per open |

### Behavior
1. App is shielded by default
2. User opens the app → shield appears showing opens used / allowed + streak
3. User taps "Open [App]" → app unshields for `timePerOpen` minutes
4. After time expires → app re-shields automatically
5. Once all daily opens are used → shield shows a stronger "are you sure?" confirmation
6. Daily reset at midnight: opens counter → 0, streak increments if limit was respected

### Streak per App
Each app intention tracks its own streak. Breaking the limit resets that app's streak to 0.

### Streak Freeze
- 1 per week (granted Monday)
- Protects ALL intention streaks for the day
- Pro-only feature
- Shield shows special "Streak Freeze Active" state

---

## 6. Screen Time Tracking & Daily Score

The app monitors total daily screen time via a background extension that fires at 30-minute threshold intervals.

### Thresholds
- 32 events at every 30 minutes (30 min → 16 hours)
- 16 "warning" events at the 50-minute mark of each hour

### Score
- `Score = 100 - (minutes / 10)` — e.g., 0 min = 100, 500 min = 50

### Daily Screen Time Goal
- User sets a goal like "under 3 hours"
- Streak increments if user stays under goal
- **Lives system**: absorbs goal-breaking days without breaking streak. Extra life on Sundays.

### Daily Review
- Next-day view showing yesterday's screen time
- User rates their usage (thumbs up/down)
- Uses native `DeviceActivityReport` embedded in the app

---

## 7. Widgets

### Lock Screen Widget (Session Widget)
- Shows during active session: progress bar + session name
- Shows when idle: "Start Session" CTA
- Tapping deep-links into the app

### Home Screen Widget (Streak Widget)
- Small widget showing current streak number + flame icon
- Shows screen time goal text
- Rotating streak-themed background images
- Refreshes every 30 minutes

---

## 8. Live Activities (Lock Screen Banner)

During an active session, a **persistent lock screen banner** shows:
- Timer counting up toward the goal
- Session name
- On completion: celebration message with XP earned
- On give up: shows how long user lasted

---

## 9. XP, Coins & Gamification

### Points/XP per Session
| Duration | XP |
|---|---|
| ≤ 15 min | 3 |
| ≤ 30 min | 5 |
| ≤ 45 min | 8 |
| ≤ 60 min | 10 |
| ≤ 90 min | 15 |
| ≤ 120 min | 25 |

### Coins
Same scale as XP. Earned on session completion.

### Screen Time Points (daily)
Tiered: < 1h = 100 pts, < 2h = 75, ... 8h+ = 0

### Leaderboard
- Weekly competition cycles (Mon–Sun)
- Tiers: bronze → silver → gold → platinum → diamond
- Notification on Monday when leaderboard finalizes

---

## 10. Notifications

| Trigger | Message | When |
|---|---|---|
| Session goal reached | "Congrats! You completed your Present Session" | At goal duration |
| Scheduled session start | "Starting [Session Name]" | At schedule time |
| App intention open | "Opening the app for X minutes" | On timed open |
| App intention closing | "Closing the app in 30 seconds" | 30s before time expires |
| Daily report ready | "Your Daily Review is ready" | 9 AM next day |
| Morning session prompt | "Start tomorrow without scrolling" | 7 PM, throttled |
| Accountability partner | "Add an accountability partner…" | 5 PM, certain days |
| Leaderboard finalized | "Leaderboard finalized!" | Mondays 5 PM |
| Screen time quips | 100+ witty/sarcastic messages | At each 30-min threshold |

---

## 11. Quick Actions (Force Touch / Long Press)

Two home screen shortcuts:
- **Unblock**: If session active → opens end-session flow. If no session → clears all shields immediately
- **Feedback**: Opens in-app feedback form

---

## 12. Accountability Partners

When a user breaks an app intention streak, a message is automatically sent to their registered accountability partners.

---

## 13. Session Tutorial (First-Time Experience)

Experiment-gated onboarding flow:
1. Shields all apps immediately (no duration)
2. Shield shows "Open BePresent to finish your first session"
3. User returns to app → tutorial completes → awards 5 XP

---

## 14. Session Stats

- Tracks completed sessions per day (last 30 days)
- Personal record for longest session time in a day
- Session history: name, goal duration, extra time

---

## 15. Screen Time Authorization

- Requests `FamilyControls` authorization (`.individual` mode — no parental controls)
- Handles missing passcode and missing iCloud sign-in errors
- Re-validates on each app launch; re-onboards if authorization was revoked
