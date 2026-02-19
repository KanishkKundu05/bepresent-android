# Blocked Time Idle Card

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/BlockedTimeView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/BlockedTimeViewModel.swift`

## What it looks like
- Title: "Time Blocked today".
- Daily record chip.
- Large HH:MM:SS timer block.
- Two capsule pickers:
  - Block mode picker entry.
  - Duration picker entry.
- Primary CTA: "Block Now".

## Minor UI components and snippets

### 1) Card structure
```swift
VStack(spacing: 0) {
    Text("Time Blocked today")
    dailyRecordView
    timerView
    HStack(spacing: 15) {
        sessionModeButton
        sessionGoalButton
    }
    FullButton(configuration: .init(title: "Block Now", icon: .systemImage("play.fill"))) { ... }
}
```

### 2) Daily record chip
```swift
Text("Your daily record: \(dailyRecord.hours)h \(dailyRecord.minutes)m \(dailyRecord.seconds)s")
    .font(theme.font.subLabel)
    .foregroundStyle(theme.color.brandPrimary)
    .padding(.vertical, 6)
    .padding(.horizontal, 16)
    .background(theme.color.brand100)
    .clipShape(Capsule())
```

### 3) Timer digits row
```swift
HStack(alignment: .top, spacing: 4) {
    VStack { DigitView(digit: viewModel.currentTime.hours, isDisabled: isDisabled); Text("Hours") }
    Text(":")
    VStack { DigitView(digit: viewModel.currentTime.minutes, isDisabled: isDisabled); Text("Minutes") }
    Text(":")
    VStack { DigitView(digit: viewModel.currentTime.seconds, isDisabled: isDisabled); Text("Seconds") }
}
```

### 4) Session mode entry capsule
```swift
HStack {
    Image("session-mode")
    Text(viewModel.sessionMode == .allowList ? "All apps" : "Specific apps")
    Spacer()
    Image(systemName: "chevron.right")
}
.padding(.vertical, 8)
.padding(.horizontal, 12)
.background(Capsule().fill(.ultraThinMaterial))
```

### 5) Session duration entry capsule
```swift
HStack {
    Image("session-timer")
    Text(viewModel.sessionDuration)
    Image(systemName: "chevron.right")
}
.padding(.vertical, 8)
.padding(.horizontal, 12)
.background(Capsule().fill(.ultraThinMaterial))
```

### 6) Block Now action and validation
```swift
FullButton(configuration: .init(title: "Block Now", icon: .systemImage("play.fill"))) {
    if viewModel.validateSession() {
        NotificationCenter.default.post(name: HomeV2ViewModel.startSessionCountDown, object: nil)
    }
}
```

## Behavior references
- Beast mode validation and confirm alert.
- Empty block-list validation alert.
- Animated stat refresh after Claim XP dismiss.
- Session mode/duration refresh after sheet save.

## Related docs
- `swift/Screentox/HOMEV2_PORTING/04A_SESSION_MODE_SHEET.md`
- `swift/Screentox/HOMEV2_PORTING/04B_SESSION_GOAL_SHEET.md`

## Kotlin/Compose mapping
- Build `BlockedTimeCard` with stateless UI + callbacks.
- Keep validation logic in `HomeV2ViewModel`/session domain layer.
- Keep capsule entries visually translucent and pill-shaped.

