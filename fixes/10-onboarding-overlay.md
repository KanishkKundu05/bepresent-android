# Fix 10: No Overlay Permission in Onboarding Flow

## Status: FIXED

## Problem
Even after adding `SYSTEM_ALERT_WINDOW` to the manifest (Fix 06), the user needs to grant the permission at runtime via system settings. The onboarding flow didn't include this step, so users would complete onboarding without overlay permission — and the shield would be silently suppressed by the OS.

## File
`ui/onboarding/OnboardingScreen.kt`

## Fix Applied
1. Added `STEP_OVERLAY = 4` between battery and done steps
2. Added overlay permission check (`overlayGranted` state) with lifecycle-aware re-check
3. Added overlay step UI with:
   - Explanation of why the permission is needed
   - Button to open system overlay settings
   - Skip option (non-critical but strongly recommended)
4. Battery step now transitions to `STEP_OVERLAY` instead of `STEP_DONE`
