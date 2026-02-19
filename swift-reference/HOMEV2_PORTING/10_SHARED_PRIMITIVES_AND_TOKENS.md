# Shared Primitives And Tokens (Required Before Porting HomeV2)

## Primary Swift files
- `swift/Screentox/Screentox/Core/Theme/Theme.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/HomeV2+Extensions.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/FullButton.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/CustomPickerView.swift`
- `swift/Screentox/Screentox/Core/UI/New Components/CustomProgressBar.swift`
- `swift/Screentox/Screentox/Features/Sessions/AppIconsListView.swift`

## Critical tokens
- `brandPrimary`: `#003BFF`
- `blue1`: `#003BFF`
- `blue2`: `#55B7FF`
- `blue3`: `#ABDDFF`
- `neutral100`: `#F9F9F9`
- `neutral200`: `#E6E6E6`
- `yellowFill`: `#FFF9E5`

## Minor UI component contracts and snippets

### 1) `cardV2` shell
```swift
self
    .background(theme.color.neutral100)
    .clipShape(RoundedRectangle(cornerRadius: 24))
    .background {
        RoundedRectangle(cornerRadius: 24)
            .fill(theme.color.neutralBlack.opacity(0.1))
            .offset(y: 2)
    }
    .shadow(color: theme.color.neutralBlack.opacity(0.15), radius: 15, x: 0, y: 4)
```

### 2) `backgroundV2` recipe
```swift
Rectangle()
    .fill(RadialGradient(colors: colors, center: .center, startRadius: 0, endRadius: 400))
    .ignoresSafeArea()
    .mask {
        Circle()
            .frame(width: UIScreen.main.bounds.height, height: UIScreen.main.bounds.height)
            .offset(y: -UIScreen.main.bounds.height/2)
    }
```

### 3) `FullButton` capsule with optional drop-shadow layer
```swift
ZStack {
    if let dropShadowColor {
        Capsule().fill(dropShadowColor).offset(y: 4)
    }
    Capsule().fill(backgroundColor)
}
```

### 4) Segmented control style picker
```swift
HStack(spacing: 0) {
    ForEach(Array(options.enumerated()), id: \.element) { option in
        Text(option.element).onTapGesture { selectedIndex = option.offset }
    }
}
.background { Capsule().fill(theme.color.neutral200) }
```

### 5) Progress bar primitive
```swift
CustomProgressBar(
    backgroundColor: ...,
    filledColor: ...,
    height: 8,
    progress: progress
)
```

### 6) App icon stack primitive
```swift
HStack(spacing: -12) {
    ForEach(Array(appsList.prefix(totalAppsCount).enumerated()), id: \.element) { index, element in
        Label(element).labelStyle(.iconOnly).rotationEffect(.degrees(index.isMultiple(of: 2) ? -5 : 5))
    }
}
```

## Kotlin/Compose mapping
- Implement these as reusable Compose building blocks before screen-level work.
- HomeV2 visual parity depends on these primitives.

