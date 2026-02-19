# Date Carousel (Above "Time Blocked")

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Calendar/CalendarDayView.swift`
- Integration point: `swift/Screentox/Screentox/Screens/HomeV2/HomeV2View.swift`

## What it looks like
- A 7-item day strip centered above the main card.
- Visual arc effect: each day tile has custom rotation and Y offset.
- Current day is center item (index 3) with stronger styling.
- Each day contains:
  - weekday + date
  - circular status marker (checked/unchecked/disabled).

## Minor UI components and snippets

### 1) Render only when idle (not actively blocking)
```swift
if !viewModel.isBlockingActive {
    CalendarView()
        .padding(.top)
        .padding(.bottom, -10)
}
```

### 2) Calendar view shell (non-interactive, centered)
```swift
ScrollView(.horizontal) {
    daysView
}
.defaultScrollAnchor(.center)
.scrollDisabled(true)
```

### 3) Arc transform logic (the key visual recipe)
```swift
.padding(.horizontal, [6, 5, 2, 2, 2, 5, 6][safe: index] ?? 0)
.offset(y: [15, -10, -25, -25, -25, -10, 15][safe: index] ?? 0)
.rotationEffect(.degrees([-18, -15, -10, 0, 10, 15, 18][safe: index] ?? 0))
```

### 4) Data generation for the 7-day window
```swift
let distances = [-3, -2, -1, 0, 1, 2, 3]
days = distances
    .map { Date.now.addingTimeInterval(TimeInterval(86_400 * $0)) }
    .map { date in
        // produce Day(weekDay, number, isEnabled, isChecked)
    }
```

### 5) Current-day marker logic
```swift
CalendarDayView(
    day: element.number,
    weekday: element.weekDay,
    isCurrentDay: index == 3,
    isEnabled: element.isEnabled,
    isChecked: element.isChecked
)
```

### 6) Day cell variants (enabled/current/checked)
```swift
if isEnabled {
    if isCurrentDay {
        dataBody.background(theme.color.neutralWhite).clipShape(Capsule())
    } else {
        dataBody.background(.thinMaterial.opacity(0.8)).clipShape(Capsule())
    }
} else {
    dataBody
        .background(theme.color.neutralWhite.opacity(0.15))
        .clipShape(Capsule())
}
```

## Kotlin/Compose mapping
- Build `HomeDateCarousel(days: List<DayUiModel>)`.
- Recreate arc effect via fixed transform arrays by item index.
- Keep center day index at 3.
- Recreate status circle variants:
  - disabled outlined
  - enabled gray
  - enabled green checked.

