# Daily Quest Card

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestV2View.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestV2ViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/DailyQuest/DailyQuestItemView.swift`

## What it looks like
- Header with title and progress count (`x/3 complete`).
- Horizontal progress bar.
- Three checklist rows and one rewards row.

## Minor UI components and snippets

### 1) Header and progress
```swift
HStack {
    Text("Daily Quest")
    Spacer()
    Text("\(viewModel.completedCount)/3 complete")
}

CustomProgressBar(
    backgroundColor: theme.color.brand300.opacity(0.1),
    filledColor: theme.color.brand300,
    height: 8,
    progress: Double(viewModel.completedCount) / 3.0
)
```

### 2) Checklist rows
```swift
DailyQuestItemView(title: "Yesterday’s Review", isCompleted: viewModel.completedReview)
DailyQuestItemView(title: "Tip of the Day", isCompleted: viewModel.completedTip)
DailyQuestItemView(title: "Present Session", isCompleted: viewModel.completedSession)
```

### 3) Rewards row
```swift
DailyQuestItemView(
    title: "Rewards",
    isCompleted: false,
    emojiIcon: "🎁",
    customIconBackground: theme.color.yellowPrimary.opacity(0.5)
)
```

## Behavior notes
- Completion status comes from app/session state bindings.
- Row taps open daily report, tip sheet, session flow, rewards sheet.

## Kotlin/Compose mapping
- Build from immutable `DailyQuestUiState`.
- Keep row semantics/tap targets matching iOS.

