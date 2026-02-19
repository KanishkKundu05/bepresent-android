# Active Session Card (Includes Break Mode)

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/ActiveSessionV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/ActiveSessionV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/BreakDurationPickerView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Clouds/DriftingClouds.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Clouds/Cloud.swift`

## What it looks like
- Cloud background + drifting cloud animation.
- Center icon area (`session-brick`, optional rotating burst on completion).
- Session title area.
- Translucent allow/block list chip.
- Main timer + progress capsule + XP text.
- Break controls (`Take A Break`, break timer, `End Break Now`) when break is running.
- Finish action area (`Give Up` or Beast Mode lock style).

## Minor UI components and snippets

### 1) Background layer
```swift
Color.clear.overlay {
    Image("cloud-bg")
        .resizable()
        .scaledToFill()

    if !viewModel.completed {
        DriftingClouds()
    }
}
```

### 2) Center icon + complete burst
```swift
if viewModel.completed {
    Image("session-burst")
        .opacity(0.6)
        .rotationEffect(.degrees(viewModel.burstRotation))
}
Image("session-brick")
    .resizable()
    .frame(width: 126, height: 126)
```

### 3) Session title switching
```swift
var sessionTitle: String {
    if completed { return "Session Complete!" }
    if breakRunning { return "On a Break" }
    return sessionName
}
```

### 4) Translucent allow/block list picker chip
```swift
HStack {
    Text(viewModel.mode == .blockList ? "Block List" : "Allow List")
    AppIconsListView(...)
    Image(systemName: "chevron.down")
}
.padding(.horizontal, 20)
.frame(height: 45)
.background(
    Capsule()
        .fill(theme.color.neutralWhite.opacity(0.35))
        .overlay(Capsule().stroke(theme.color.neutralWhite, lineWidth: 1))
)
```

### 5) Main timer + progress capsule
```swift
Text(viewModel.timeRemainingString)
    .font(theme.font.title)
    .monospacedDigit()

HStack(spacing: 4) {
    CustomProgressBar(
        backgroundColor: theme.color.yellowPrimary.opacity(0.2),
        filledColor: theme.color.yellowPrimary,
        height: 8,
        progress: progress
    )
    Image("bolt").frame(width: 15, height: 15)
    Text("+ \(viewModel.points) XP")
}
.padding(10)
.background { Capsule().fill(theme.color.yellowFill) }
```

### 6) "Take A Break" action
```swift
FullButton(configuration: .init(
    title: "Take A Break",
    icon: .systemImage("pause.fill", weight: .bold),
    appearance: .gray
)) {
    viewModel.showBreakPicker = true
}
```

### 7) Break-running capsule + break timer + end break CTA
```swift
Text("Apps Temporarily Unlocked")
    .frame(height: 45)
    .background(
        Capsule()
            .fill(theme.color.greenFill.opacity(0.7))
            .overlay(Capsule().stroke(theme.color.greenPrimary.opacity(0.5), lineWidth: 1))
    )
```

```swift
Text(viewModel.breakTimeRemainingString).monospacedDigit()
CustomProgressBar(
    backgroundColor: theme.color.brandPrimary.opacity(0.2),
    filledColor: theme.color.brandPrimary,
    height: 8,
    progress: viewModel.breakTimeReverseProgress
)
FullButton(configuration: .init(title: "End Break Now", appearance: .gray)) {
    viewModel.endBreak()
}
```

### 8) Finish action area (`Give Up` / Beast Mode variant)
```swift
if viewModel.beastMode {
    FullButton(configuration: .init(
        title: "Beast Mode - No Giving Up",
        appearance: .dangerShadow
    )) { viewModel.presentBeastModeSheet() }
} else {
    FullButton(configuration: .init(
        title: "Give Up",
        icon: .systemImage("stop.fill", weight: .bold),
        appearance: .dangerShadow
    )) { viewModel.presentStopDialog() }
}
```

## State and behavior notes
- Break availability: enabled only when not beast mode and experiment flag is on.
- Auto-complete flow:
  - timer marks completed
  - short delay
  - session ends as completed
  - Claim XP overlay is triggered.
- Break flow:
  - start break sets end time and temporarily unshields apps
  - end break re-applies shielding.

## Kotlin/Compose mapping
- Build `ActiveSessionCard` with explicit sub-state:
  - `active`
  - `breakRunning`
  - `completedTransient`
- Keep allow-list chip translucent and rounded.
- Keep timer text monospaced.
- Keep progress capsules and button appearances as in iOS.

