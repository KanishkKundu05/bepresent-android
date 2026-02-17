# Fix 08: Back Gesture Bypasses Shield on Android 13+

## Status: FIXED

## Problem
`BlockedAppActivity` originally overrode the deprecated `onBackPressed()` method. On Android 13+ with predictive back gestures enabled, this override is not invoked — the system back gesture could return the user to the blocked app, bypassing the shield entirely.

## File
`features/blocking/BlockedAppActivity.kt`

## Fix Applied
Replaced deprecated `onBackPressed()` override with an `OnBackPressedCallback` registered on the dispatcher:

```kotlin
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        navigateHome()
    }
})
```

Back gesture now correctly sends the user home instead of back to the blocked app.
