# HomeV2 Root Screen

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2+Extensions.swift`

## What it looks like
- Header at top.
- Scrollable body with calendar (idle only), main card, intentions card, daily quest card.
- Blue radial background behind everything.
- Main card changes by state.

## Minor UI components and snippets

### 1) Root stack + header + scroll
```swift
VStack(spacing: 0) {
    HeaderView()
        .padding(.horizontal, 16)
        .padding(.top, 6)
        .padding(.bottom, 10)

    if #available(iOS 17.0, *) {
        scrollView
    } else {
        scrollViewiOS16
    }
}
```

### 2) Main card state switch
```swift
if !viewModel.isBlockingActive {
    BlockedTimeView()
} else if viewModel.isCountingDown {
    SessionCountDownView()
} else {
    ActiveSessionV2View()
}
```

### 3) Card shell and top overlay banner
```swift
mainCardView
    .cardV2(theme: theme)
    .padding(.horizontal)
    .overlay(alignment: .top) {
        if viewModel.shouldShowUpgradeBanner && !viewModel.isBlockingActive {
            WinbackBannerView(name: getUser()?.firstName.trimmingCharacters(in: .whitespacesAndNewlines))
                .offset(y: -28)
        }
    }
```

### 4) Global background
```swift
func backgroundV2(theme: Theme) -> some View {
    return backgroundV2With([theme.color.blue3, theme.color.blue2, theme.color.blue1])
}
```

## Kotlin/Compose mapping
- Build one `HomeV2Screen` composable with a single state model:
  - `isSessionActive`
  - `isCountingDown`
  - `showTutorial`
  - `showClaimXp`
- Keep conditional rendering exactly as iOS:
  - `BlockedTimeCard` when idle.
  - `SessionCountdownCard` during countdown.
  - `ActiveSessionCard` while session runs.
- Implement `cardV2` and `backgroundV2` as reusable wrappers.

