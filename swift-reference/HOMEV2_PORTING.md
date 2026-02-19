# HomeV2 iOS -> Android (Kotlin/Compose) Porting Context

## Purpose
This document is a source-of-truth map of the current iOS Home V2 screen implementation so Android can recreate the same design and behavior in Kotlin/Jetpack Compose.

It focuses on:
- Exact Swift files used by each visible feature.
- How each feature works (UI + state + triggers).
- Reusable UI primitives and theme tokens.
- Tab bar implementation and assets.
- Screenshot checklist for parity validation.

---

## Scope And States Covered
- Idle Home state (default non-blocking home).
- Countdown state (3, 2, 1 before block starts).
- Active Session state.
- Break-running state inside active session.
- Session completion + Claim XP overlay flow.
- Bottom tab navigation behavior.

---

## Root Screen Composition

### Primary files
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2+Extensions.swift`

### How it is implemented
- Home screen root structure:
  - Header at top.
  - Scroll content body below.
  - Main card switches between 3 states:
    - `BlockedTimeView` (idle).
    - `SessionCountDownView` (countdown).
    - `ActiveSessionV2View` (session running).
- iOS 17+ and iOS 16 have separate scroll implementations for layout/scroll bugs.
- Background uses a masked radial gradient (`backgroundV2`).
- Claim XP is shown via full-screen cover bound to `latestCompletedSession`.
- Session creation and tutorial are presented via sheet/full-screen cover.

### Key state switch snippet (from `HomeV2View.swift`)
```swift
if !viewModel.isBlockingActive {
    BlockedTimeView()
} else if viewModel.isCountingDown {
    SessionCountDownView()
} else {
    ActiveSessionV2View()
}
```

### Key background snippet (from `HomeV2+Extensions.swift`)
```swift
func backgroundV2(theme: Theme) -> some View {
    return backgroundV2With([theme.color.blue3, theme.color.blue2, theme.color.blue1])
}
```

---

## Theme, Tokens, And Shared Visual Contracts

### Primary files
- `swift/Screentox/Screentox/Core/Theme/Theme.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2+Extensions.swift`

### Critical tokens for parity
- Brand primary: `#003BFF`
- Blue gradient family:
  - `blue1`: `#003BFF`
  - `blue2`: `#55B7FF`
  - `blue3`: `#ABDDFF`
- Card surface: `neutral100` (`#F9F9F9`) with rounded radius 24 and drop shadow.
- Font family: `"FFF Acid Grotesk Soft VF"` across all major HomeV2 text styles.

### Compose mapping guidance
- Create a `HomeV2Theme` token object in Android with these exact colors and typography names.
- Recreate `cardV2` as a reusable modifier/composable wrapper (rounded background + subtle offset shadow layer + blur shadow).
- Recreate `backgroundV2` using a radial gradient brush clipped/masked to a top-centered large circle.

---

## Feature Inventory: What To Rebuild

## 1) Header Row (Profile, Streak, XP)

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Header/HeaderView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Header/HeaderViewModel.swift`

### UI behavior
- Left: profile icon button opens profile sheet.
- Middle: streak pill with flame + streak number.
- Right: XP pill with bolt icon + weekly XP.
- Uses live values from `AppState` (`streakReport`, `weeklyPoints`).

### Compose notes
- Build as `HomeHeaderRow`.
- Use `StateFlow`/`collectAsState` equivalent of `AppState` values.
- Keep pill sizing, corner radius, and icon/text spacing close to iOS.

---

## 2) Top Data Strip ("Carousel"-like Day Arc)

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarDayView.swift`

### UI behavior
- Horizontal row of 7 day pills around today (`-3 ... +3` days).
- Each day has enabled/disabled state and checked/unchecked ring/check.
- Uses custom offsets/rotations to create curved arc look.
- Current day has distinct capsule and color treatment.

### Data behavior
- `CalendarViewModel.generateDays()` computes day models from current date, install date, and `SessionStatsManager`.
- Regenerates when active session state changes via notification.

### Compose notes
- Build with a fixed list of 7 items, each with transform (`rotationZ` + `offsetY`) for arc.
- Create day cell variants for:
  - enabled + checked
  - enabled + unchecked
  - disabled
  - current day accent

---

## 3) Time Blocked Card (Idle Main Card)

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/BlockedTimeView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/BlockedTimeViewModel.swift`

### Visible sections
- Title: "Time Blocked today".
- Daily record chip.
- Large HH:MM:SS timer digits.
- Two capsule controls:
  - session mode (All apps / Specific apps).
  - session goal duration.
- Primary CTA: `FullButton` "Block Now" with play icon.

### Behavior details
- `Block Now` triggers validation:
  - Beast mode confirmation alert when needed.
  - Empty list alert if blocking list is invalid for mode.
- If valid, posts `HomeV2ViewModel.startSessionCountDown`.
- Timer digits animate with numeric transition and staged sequence in view model.

### Key start-session snippet
```swift
if viewModel.validateSession() {
    NotificationCenter.default.post(name: HomeV2ViewModel.startSessionCountDown, object: nil)
}
```

### Compose notes
- `BlockedTimeCard` should be a standalone composable with:
  - `AnimatedContent`/`animateIntAsState` for digit transitions.
  - Material bottom sheets for mode/goal config.
  - Alert dialogs for beast mode and empty list checks.

---

## 4) Session Mode Picker ("All Apps" / "Specific Apps")

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/SessionModeBottomSheet.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/CustomPickerView.swift`
- `swift/Screentox/Screentox/Features/Sessions/AppIconsListView.swift`

### UI behavior
- Bottom sheet title: "Block Mode".
- Segmented control style picker: All Apps / Specific Apps.
- Dynamic apps list summary row with app/category/website icons stack.
- "Set Mode" button saves mode to `PresentSessionManager.shared.defaultMode`.

### Compose notes
- Build as modal bottom sheet + custom segmented component.
- Recreate stacked icon pile (negative spacing + alternating tilt).
- Keep mode semantics identical:
  - `allowList` -> "All Apps".
  - `blockList` -> "Specific Apps".

---

## 5) Session Duration + Beast Mode Picker

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/SessionGoalBottomSheet.swift`

### UI behavior
- Bottom sheet title: "Duration".
- Duration stepper in +/- 15 minute increments, min 15m, max 2h.
- Shows projected XP for chosen duration.
- Beast mode toggle (PRO-gated logic).
- "Set Duration" persists:
  - default goal
  - default points
  - default coins
  - default beast mode

### Compose notes
- Use bottom sheet with local state and explicit save action.
- Preserve exact duration bounds and increment logic.
- Include PRO gating behavior when beast mode is locked.

---

## 6) Countdown State (Before Block Starts)

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/SessionCountDownView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2ViewModel.swift`

### UI behavior
- Background: cloud image.
- Title: "Blocking apps in".
- Animated large count: 3 -> 2 -> 1.
- Cancel button aborts and exits countdown.
- On completion posts `HomeV2ViewModel.startSession`.

### Compose notes
- Use coroutine timer + animated text transitions.
- Drive state from one source (view model) so cancel/start are deterministic.

---

## 7) Active Session State Card

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/ActiveSessionV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/ActiveSessionV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/BreakDurationPickerView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Clouds/DriftingClouds.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Clouds/Cloud.swift`

### Visible sections
- Cloud background image + drifting cloud overlay animation.
- Session icon area (`session-brick`, optional rotating `session-burst` on completion).
- Title changes by state:
  - session name
  - "On a Break"
  - "Session Complete!"
- Allow/Block list button with icons.
- Timer + progress bar + XP capsule.
- Optional break controls:
  - "Take A Break"
  - break timer bar
  - "End Break Now"
- Finish action:
  - Give Up confirmation, or beast mode locked state messaging.

### Behavior details
- Timer wiring via `SessionTimer`.
- Completion auto-ends session after brief delay and opens XP flow.
- Break flow temporarily unlocks apps, schedules notifications, then re-shields apps.

### Compose notes
- Create separate render branches for:
  - active
  - break running
  - completed transient
- Keep timer text monospaced digits.
- Use linear infinite animations for drifting clouds and burst rotation.

---

## 8) Intentions Card

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsListView.swift`

### UI behavior
- Card title: "App Intentions".
- List of current intentions.
- Reload action, add action, and footer CTA.
- Opens setup/modification flows in sheets/full screen.

### Compose notes
- Keep this as a self-contained card module with list + actions.
- Reuse card styling token from `cardV2`.

---

## 9) Daily Quest Card

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestItemView.swift`

### UI behavior
- Header with progress text (`x/3 complete`) and progress bar.
- Checklist rows:
  - Yesterday's Review
  - Tip of the Day
  - Present Session
- Rewards row.
- Opens related screens/sheets.

### Compose notes
- Implement as card with progress + list rows + click handlers.
- Bind completion flags from shared app/session state.

---

## 10) Upgrade/Winback Banner Overlay

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/WinbackBanner/WinbackBannerView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2ViewModel.swift`

### UI behavior
- Capsule gradient promo banner overlays top of main card when eligible.
- Tapping banner triggers paywall event registration.
- Visibility controlled by `shouldShowUpgradeBanner`.

### Compose notes
- Render as overlay above main card only in idle mode.
- Keep gradient colors and capsule proportions consistent.

---

## 11) Session Tutorial + Claim XP Overlays

### Files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/SessionTutorial/SessionTutorialView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/SessionTutorial/SessionTutorialManager.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ClaimXP/ClaimXPView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2View.swift`

### UI behavior
- Tutorial may block first countdown and show guided overlays/tooltips.
- Claim XP appears as full-screen cover after completed sessions.
- Dismissing Claim XP triggers notification used by blocked-time animation logic.

### Compose notes
- Model as full-screen overlays/dialog destinations controlled by centralized UI state.
- Preserve sequencing around completion -> XP popup -> timer stat update.

---

## Bottom Tab Navigation (App-Level)

### Files
- `swift/Screentox/Screentox/Screens/TabContainer/TabContainerView.swift`
- `swift/Screentox/Screentox/Screens/TabContainer/TabContainerViewModel.swift`

### Tabs and order
1. Home (`home-tab`)
2. Schedules (`schedules-tab`)
3. Leaderboard (`leaderboard-tab`)
4. Screen Time (`screen-time-tab`)
5. Social (`social-tab`)

### Behavior details
- HomeV2 sits inside `TabView` as first tab.
- Tint color comes from `theme.color.brandPrimary`.
- App state can programmatically switch selected tab and force home scroll-to-top.

### Compose notes
- Build Android bottom nav with identical tab order and labels.
- Keep icon names consistent with exported Android assets.

---

## Reusable UI Primitives Used By HomeV2

### Files
- `swift/Screentox/Screentox/Core/UI/New Components/FullButton.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/CustomPickerView.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/CustomProgressBar.swift`
- `swift/Screentox/Screentox/Features/Sessions/AppIconsListView.swift`

### Why these matter
- HomeV2 heavily depends on these shared primitives for button styling, segmented picker behavior, progress bars, and app icon list visuals.
- Recreating these as reusable Compose components reduces drift across Home states.

---

## Assets Checklist (Export/Map To Android)

### HomeV2 assets folder
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/home-tab.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/schedules-tab.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/leaderboard-tab.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/screen-time-tab.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/social-tab.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/profile-icon.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/bolt.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/session-mode.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/session-timer.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/checkmark.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/HomeV2/cloud-bg.imageset`

### Additional non-HomeV2-named assets used in HomeV2
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/session-burst.imageset`
- `swift/Screentox/Screentox/Core/UI/Assets.xcassets/session-brick.imageset`

---

## Screenshot Placeholder Checklist

Add these files when captures are available, then embed in Android port docs:

1. `screenshots/homev2/01-idle-home.png`
- Show: header, calendar arc, blocked timer, mode picker, goal picker, block now button, intentions card top, daily quest top, bottom tabs.

2. `screenshots/homev2/02-session-mode-sheet.png`
- Show: block mode sheet with picker and apps row.

3. `screenshots/homev2/03-session-goal-sheet.png`
- Show: duration stepper, XP projection, beast mode toggle.

4. `screenshots/homev2/04-countdown.png`
- Show: "Blocking apps in" with count.

5. `screenshots/homev2/05-active-session.png`
- Show: active timer, progress bar, allow/block list chip, give up/break actions.

6. `screenshots/homev2/06-break-running.png`
- Show: apps unlocked state + break timer and end break CTA.

7. `screenshots/homev2/07-tabs-default.png`
- Show: full tab bar with Home selected.

---

## Suggested Compose Module Breakdown

- `HomeV2Screen`
- `HomeV2ViewModel`
- `HomeHeaderRow`
- `HomeCalendarArc`
- `BlockedTimeCard`
- `SessionModeBottomSheet`
- `SessionGoalBottomSheet`
- `SessionCountdownCard`
- `ActiveSessionCard`
- `IntentionsCard`
- `DailyQuestCard`
- `HomeUpgradeBanner`
- `HomeBottomNav` (app-level integration)

Keep each composable API aligned with iOS state inputs (active/countdown/completed/break-running) to reduce behavior mismatch.

---

## Acceptance Criteria For Android Parity

- Idle Home matches iOS layout hierarchy and spacing structure.
- Card and background visuals match iOS token recipes (`cardV2`, `backgroundV2`).
- Mode/goal pickers follow same semantics and persistence behavior.
- Countdown and active session transitions map to same logical triggers.
- Bottom tabs match order, labels, and icon mapping.
- Screenshot checklist can be completed with one-to-one UI correspondence.

