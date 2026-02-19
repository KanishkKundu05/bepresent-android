# Session Mode Bottom Sheet

## Primary Swift file
- `swift/Screentox/Screentox/Screens/HomeV2/Components/BlockedTime/SessionModeBottomSheet.swift`

## What it looks like
- Sheet title: "Block Mode".
- Segmented selector:
  - All Apps
  - Specific Apps
- App-list summary row with stacked app/category/website icons.
- Primary action button: "Set Mode".

## Minor UI components and snippets

### 1) Segmented picker
```swift
CustomPickerView(
    options: ["All Apps", "Specific Apps"],
    selectedIndex: $selectedIndex
)
```

### 2) Apps list summary row
```swift
HStack {
    Text(selectedIndex == 0 ? "Allowed Apps" : "Blocked Apps")
    Spacer()
    AppIconsListView(
        appsList: appsList,
        categoriesList: categoriesList,
        websitesList: websitesList,
        placeholderColor: theme.color.brand300
    )
    Image(systemName: "chevron.down")
}
```

### 3) Save mode action
```swift
private func setMode() {
    let mode: SessionMode = selectedIndex == 0 ? .allowList : .blockList
    PresentSessionManager.shared.defaultMode = mode
    didSetData()
    dismiss()
}
```

## Kotlin/Compose mapping
- Use modal bottom sheet with local `selectedIndex`.
- Keep naming parity:
  - index `0` -> `allowList` -> "All Apps".
  - index `1` -> `blockList` -> "Specific Apps".
- Keep icons summary row tappable to open app selection flow.

