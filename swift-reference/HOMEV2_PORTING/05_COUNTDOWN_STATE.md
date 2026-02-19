# Countdown State Card

## Primary Swift file
- `swift/Screentox/Screentox/Screens/HomeV2/Components/ActiveSession/SessionCountDownView.swift`

## Trigger points
- Entered from idle card when Block Now passes validation:
  - Notification: `HomeV2ViewModel.startSessionCountDown`.
- Exited by cancel or countdown completion.

## What it looks like
- Cloud background image.
- Title: "Blocking apps in".
- Large animated numeric countdown (`3,2,1`).
- Bottom cancel CTA.

## Minor UI components and snippets

### 1) Static background
```swift
Color.clear.overlay {
    Image("cloud-bg")
        .resizable()
        .scaledToFill()
}
```

### 2) Countdown number
```swift
Text("\(count)")
    .font(.system(size: 96, weight: .bold))
    .transition(.asymmetric(
        insertion: .scale(scale: 0.5).combined(with: .opacity),
        removal: .opacity
    ))
    .id(count)
```

### 3) Cancel CTA
```swift
FullButton(configuration: .init(title: "Cancel", icon: .systemImage("xmark"), appearance: .plain)) {
    timer?.invalidate()
    NotificationCenter.default.post(name: HomeV2ViewModel.stopSessionCountDown, object: nil)
}
```

### 4) Auto-start session when countdown finishes
```swift
if count > 1 {
    withAnimation { count -= 1 }
} else {
    timer.invalidate()
    NotificationCenter.default.post(name: HomeV2ViewModel.startSession, object: nil)
}
```

## Kotlin/Compose mapping
- Keep countdown logic in VM/coroutine, not inside composable local timers if possible.
- Keep one cancel action that returns to idle state immediately.

