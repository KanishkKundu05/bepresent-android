# Fix 06: Missing SYSTEM_ALERT_WINDOW Permission

## Status: FIXED

## Problem
The shield is launched via `startActivity()` from `MonitoringService` (a foreground service). On Android 10+, background activity launch is restricted. Without `SYSTEM_ALERT_WINDOW`, the OS silently suppresses the `startActivity()` call — the shield never appears even when a blocked app is correctly detected.

## Files
- `AndroidManifest.xml`
- `permissions/PermissionManager.kt`

## Fix Applied
1. Added `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />` to manifest
2. Added `hasOverlayPermission()` and `getOverlayPermissionIntent()` to `PermissionManager`
3. Added `overlay` field to `PermissionStatus` data class
