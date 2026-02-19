# Header Component (Profile / Streak / XP)

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Header/HeaderView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Header/HeaderViewModel.swift`

## What it looks like
- Left circular profile icon.
- Middle streak pill (flame + number).
- Right XP pill (bolt + weekly XP count).

## Minor UI components and snippets

### 1) Header row container
```swift
HStack(spacing: 8) {
    profileButton
    Spacer()
    streakButton
    xpButton
}
```

### 2) Profile button
```swift
Image("profile-icon")
    .resizable()
    .frame(width: 36, height: 36)
    .onTapGesture {
        viewModel.showProfile = true
    }
```

### 3) Streak pill
```swift
HStack {
    FlameView(size: .extraSmall, color: isStreakFrozen ? .blue : .orange)
    Text("\(viewModel.streakReport.finalStreak)")
}
.padding(.vertical, 8)
.padding(.horizontal, 15)
.background(isStreakFrozen ? theme.color.brand100 : theme.color.orangeFill)
.cornerRadius(25)
```

### 4) XP pill
```swift
HStack {
    Image("bolt").resizable().frame(width: 16, height: 16)
    Text("\(viewModel.weeklyPoints) XP")
}
.padding(.vertical, 8)
.padding(.horizontal, 15)
.background(theme.color.yellowFill)
.cornerRadius(25)
```

## Data source
- `HeaderViewModel` binds from `AppState.shared`:
  - `streakReport`
  - `weeklyPoints`

## Kotlin/Compose mapping
- `HomeHeaderRow(profile, streak, weeklyXp, onProfile, onStreak, onXp)`.
- Recreate pills as rounded chips with exact vertical/horizontal padding.

