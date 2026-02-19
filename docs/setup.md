# Setup Guide

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

## Getting Started

1. **Clone and open**
   ```bash
   git clone https://github.com/KanishkKundu05/bepresent-android.git
   cd bepresent-android
   ```
   Open the `android/` directory in Android Studio.

2. **Sync Gradle**
   Android Studio will prompt to sync. If the Gradle wrapper JAR is missing, run:
   ```bash
   gradle wrapper --gradle-version 8.5
   ```

3. **Build and run**
   Select an emulator or device (API 26+) and hit Run.

## First Launch
The app opens with a permissions onboarding flow centered on block enforcement:
1. Overlay permission (`SYSTEM_ALERT_WINDOW`) via system settings
2. Usage Access permission (`PACKAGE_USAGE_STATS`) via system settings
3. Accessibility service enablement via system settings
4. On success, onboarding completes and opens dashboard

## Key Permissions

| Permission | How to Grant | Required? |
|---|---|---|
| Overlay | Settings > Display over other apps > BePresent | Yes (critical gate) |
| Usage Access | Settings > Usage Access > BePresent | Yes (critical gate) |
| Accessibility Service | Settings > Accessibility > BePresent | Yes (critical gate) |
| Notifications | System notification settings | Recommended |
| Battery Optimization Ignore | System battery settings | Recommended |

## Testing on Emulator
- **Usage Access**: Works on emulator, but screen time data may be minimal
- **Foreground detection**: Open another app, then reopen it — the shield should appear within ~1 second
- **Sessions**: Start a short (5 min) session to test the full lifecycle
- **Intentions**: Create an intention, then open the target app to see the shield

## Project Structure
See [architecture.md](architecture.md) for the full architecture overview.
