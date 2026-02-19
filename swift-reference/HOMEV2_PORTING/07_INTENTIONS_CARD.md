# Intentions Card

## Primary Swift files
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsView.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsViewModel.swift`
- `swift/Screentox/Screentox/Screens/HomeV2/Components/Intentions/IntentionsListView.swift`

## What it looks like
- Card title row: "App Intentions".
- Reload icon/checkmark state.
- Add icon button.
- Intention list body.
- Dashed "Add App Intention" footer.

## Minor UI components and snippets

### 1) Header row
```swift
HStack {
    Text("App Intentions")
    Button { viewModel.showReloadIntentionsAlert() } label: { ... }
    Spacer()
    Button { viewModel.showAppIntentionSetup = true } label: {
        Image(systemName: "plus.capsule.fill")
    }
}
```

### 2) Intention list body
```swift
IntentionListView(
    intentions: viewModel.intentions,
    areIntentionsFrozen: viewModel.areIntentionsFrozen,
    scrollToId: $viewModel.scrollToId
) { selectedAppIntention in
    viewModel.selectedAppIntention = selectedAppIntention
}
```

### 3) Footer CTA
```swift
HStack {
    Spacer()
    Image(systemName: "plus")
    Text("Add App Intention")
    Spacer()
}
.background(theme.color.neutralWhite)
.clipShape(RoundedRectangle(cornerRadius: 12))
.overlay {
    RoundedRectangle(cornerRadius: 12)
        .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [12,4]))
}
```

## Kotlin/Compose mapping
- Reuse `cardV2` shell.
- Include dashed-border footer card style.
- Keep reload icon animation and add button placement.

