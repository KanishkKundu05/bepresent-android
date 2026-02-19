# Permissions Module

Handles Android permission checks and system intents for BePresent's core functionality.

## Files

```
permissions/
├── PermissionManager.kt    # Permission checking and intent creation
└── OemBatteryGuide.kt      # OEM-specific battery optimization instructions
```

## PermissionManager

Singleton class that checks and manages permissions needed by onboarding and runtime checks.

### Required Permissions

| Permission | Purpose | Check Method |
|------------|---------|--------------|
| Overlay | Show blocking UI over distracting apps | `hasOverlayPermission()` |
| Usage Stats | Monitor app usage, detect foreground apps | `hasUsageStatsPermission()` |
| Accessibility | Detect active app for blocking flow | `hasAccessibilityPermission()` |
| Notifications | Show session alerts and warnings | `hasNotificationPermission()` |
| Battery Optimization | Keep monitoring service alive | `isBatteryOptimizationDisabled()` |

### Permission Status

```kotlin
data class PermissionStatus(
    val usageStats: Boolean,
    val notifications: Boolean,
    val batteryOptimization: Boolean,
    val overlay: Boolean,
    val accessibility: Boolean
) {
    val allGranted: Boolean       // All supported permissions granted
    val criticalGranted: Boolean  // Overlay + usage + accessibility
}
```

### Intent Helpers

| Method | System Settings Intent |
|--------|----------------------|
| `getOverlayPermissionIntent()` | `ACTION_MANAGE_OVERLAY_PERMISSION` |
| `getUsageAccessIntent()` | `ACTION_USAGE_ACCESS_SETTINGS` |
| `getAccessibilitySettingsIntent()` | `ACTION_ACCESSIBILITY_SETTINGS` |
| `getBatteryOptimizationIntent()` | `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` |
| `getAppSettingsIntent()` | `ACTION_APPLICATION_DETAILS_SETTINGS` |

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Startup                               │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │  PermissionManager    │
              │    .checkAll()        │
              └───────────┬───────────┘
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ criticalGranted │     │ !criticalGranted│
    │      true       │     │     false       │
    └────────┬────────┘     └────────┬────────┘
             │                       │
             ▼                       ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ DashboardScreen │     │ OnboardingScreen│
    │ (normal flow)   │     │ (request perms) │
    └─────────────────┘     └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │ User grants via │
                            │ system settings │
                            └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │ Re-check status │
                            │ on resume       │
                            └─────────────────┘
```

## OemBatteryGuide

Provides manufacturer-specific instructions for disabling aggressive battery optimization:

| Manufacturer | Key Setting Path |
|--------------|------------------|
| Xiaomi/Redmi/POCO | Battery Saver > No restrictions |
| Huawei/Honor | App Launch > Manual (all toggles ON) |
| Samsung | Background usage limits > Never sleeping apps |
| Oppo/Realme | App Quick Freeze > disable |
| OnePlus | Battery Optimization > Don't optimize |
| Vivo | Background Power Consumption > Off |

### Usage

```kotlin
val guide = OemBatteryGuide.getInstructions()
guide?.let { instruction ->
    // Display instruction.manufacturer + instruction.steps
}
// Returns null for stock Android or unknown OEMs
```

## Integration Points

1. **OnboardingScreen** - Checks permissions, shows request UI, navigates to system settings
2. **DashboardScreen** - Shows warning banner if `!permissionsOk`
3. **MonitoringService** - Requires usage stats permission to function
4. **UsageStatsRepository** - Operations fail silently without permission

## Android Version Handling

```kotlin
fun needsNotificationPermissionRequest(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU  // Android 13+
}
```

For Android 12 and below, notification permission is granted by default.
