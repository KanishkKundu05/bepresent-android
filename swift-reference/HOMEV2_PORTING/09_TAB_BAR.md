# Bottom Tab Bar

## Primary Swift files
- `swift/Screentox/Screentox/Screens/TabContainer/TabContainerView.swift`
- `swift/Screentox/Screentox/Screens/TabContainer/TabContainerViewModel.swift`

## Tab order and labels
1. Home
2. Schedules
3. Leaderboard
4. Screen Time
5. Social

## Minor UI components and snippets

### 1) Tab container
```swift
TabView(selection: $viewModel.selectedTab) {
    HomeV2View().tabItem { Image("home-tab"); Text("Home") }.tag(TabContainerViewModel.Tabs.home)
    ScheduledSessionsV2View().tabItem { Image("schedules-tab"); Text("Schedules") }.tag(.schedules)
    GlobalLeaderboardView().tabItem { Image("leaderboard-tab"); Text("Leaderboard") }.tag(.leaderboard)
    AppsReportScreenView().tabItem { Image("screen-time-tab"); Text("Screen Time") }.tag(.screenTime)
    SocialView().tabItem { Image("social-tab"); Text("Social") }.tag(.social)
}
.tint(theme.color.brandPrimary)
```

### 2) Programmatic tab changes from app state
```swift
AppState.shared.$showActivePresentSessionSheet
    .filter { $0 }
    .sink { [weak self] _ in
        self?.selectedTab = .home
        AppState.shared.scrollHomeToTop = true
    }
```

## Kotlin/Compose mapping
- Use `NavigationBar` with matching tab order and icons.
- Keep selected tint color mapped from `brandPrimary`.
- Support programmatic jump to Home + scroll-to-top behavior.

