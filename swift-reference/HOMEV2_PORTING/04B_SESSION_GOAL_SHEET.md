# Session Goal Bottom Sheet (Duration + Beast Mode)

## Primary Swift file
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/SessionGoalBottomSheet.swift`

## What it looks like
- Sheet title: "Duration".
- Centered duration with +/- circular controls.
- XP preview chip ("+X XP").
- Beast Mode row with PRO badge and toggle.
- Primary action button: "Set Duration".

## Minor UI components and snippets

### 1) Duration stepper (15 minute increments)
```swift
Button { goal = max(15 * 60, goal - 15 * 60) } label: { Text("-") ... }
Text("\(formatter.string(from: goal) ?? "30m")")
Button { goal = min(2 * 3600, goal + 15 * 60) } label: { Text("+") ... }
```

### 2) XP projection row
```swift
HStack(spacing: 5) {
    Image("bolt").resizable().frame(width: 15, height: 15)
    Text("+\(points) XP")
}
.padding(.vertical, 6)
.padding(.horizontal, 12)
.background(Capsule().fill(theme.color.yellowFill))
```

### 3) Beast mode row
```swift
HStack(spacing: 8) {
    Text("PRO")
    Text("Beast Mode")
}
Toggle("", isOn: $isBeastModeOn)
```

### 4) Persist defaults on save
```swift
PresentSessionManager.shared.defaultGoal = goal
PresentSessionManager.shared.defaultPoints = points
PresentSessionManager.shared.defaultCoins = coinsFromPresentSessionGoal(goal: goal)
PresentSessionManager.shared.defaultBeastMode = isBeastModeOn
```

## Kotlin/Compose mapping
- Implement min/max constraints: 15m to 2h.
- Keep increment size at 15m.
- Keep XP chip visible and updated on every duration change.
- Keep Beast Mode PRO-gate behavior.

