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
The app opens with a 5-step onboarding flow:
1. Welcome screen
2. Usage Access permission (required — opens system Settings)
3. Notification permission (Android 13+, can skip)
4. Battery optimization (recommended, with OEM-specific guide)
5. Ready screen → opens dashboard

## Key Permissions

| Permission | How to Grant | Required? |
|---|---|---|
| Usage Access | Settings > Usage Access > BePresent | Yes (core) |
| Notifications | Runtime dialog (Android 13+) | Recommended |
| Battery Optimization | System dialog + OEM settings | Recommended |
| All others | Auto-granted via manifest | Yes |

## Testing on Emulator
- **Usage Access**: Works on emulator, but screen time data may be minimal
- **Foreground detection**: Open another app, then reopen it — the shield should appear within ~1 second
- **Sessions**: Start a short (5 min) session to test the full lifecycle
- **Intentions**: Create an intention, then open the target app to see the shield

## Project Structure
See [architecture.md](architecture.md) for the full architecture overview.
