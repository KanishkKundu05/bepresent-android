# Android Migration Plan: Program Overview

## Goal
Build a production Android app that reaches feature parity for BePresent core value loops (focus sessions, streaks, social accountability, analytics, and subscriptions) while handling iOS-only capabilities through Android-native alternatives.

## Current Codebase Snapshot

Primary app and backend surfaces to migrate:

- iOS app entry and lifecycle: `swift/Screentox/Screentox/ScreentoxApp.swift`
- Global state and tab routing: `swift/Screentox/Screentox/Core/Models/AppState.swift`, `swift/Screentox/Screentox/Screens/TabContainer/TabContainerView.swift`
- Data sync and persistence: `swift/Screentox/Screentox/Core/Storage/DataController.swift`, `swift/Screentox/Screentox/Core/Storage/Defaults.swift`
- API layer: `swift/Screentox/Screentox/Core/Networking/Network.swift`, `swift/Screentox/Screentox/Core/Networking/New/Routes.swift`
- Sessions and app intentions: `swift/Screentox/Screentox/Features/Sessions/PresentSessionManager.swift`, `swift/Screentox/Screentox/Features/AppIntentions/V2/AppLimitManagerV2.swift`
- iOS-specific blocking/reporting extensions: `swift/Screentox/ScreentoxMonitorExtension/DeviceActivityMonitorExtension.swift`, `swift/Screentox/PresentShieldActionExtension/ShieldActionExtension.swift`, `swift/Screentox/ScreentoxReportExtension/ScreentoxReportExtension.swift`
- Widgets/live activities: `swift/Screentox/SessionWidget/SessionWidget.swift`, `swift/Screentox/StreakWidget/StreakWidget.swift`, `swift/Screentox/PresentWidgetExtension/PresentWidgetExtensionLiveActivity.swift`
- Backend routes: `server/app.ts`, `server/routes/*`

## Target Android Stack

- Language: Kotlin
- UI: Jetpack Compose + Navigation Compose
- DI: Hilt
- Async/state: Coroutines + StateFlow
- Persistence: Room + DataStore + EncryptedSharedPreferences
- Networking: Retrofit + OkHttp + Kotlinx Serialization or Moshi
- Background work: WorkManager + Foreground Service + AlarmManager (exact alarms only where required)
- Push: FCM
- Subscriptions/paywalls: Superwall + RevenueCat Android SDKs

## Migration Constraints That Must Be Decided Early

1. Hard app blocking on Android is not a direct equivalent of iOS Screen Time APIs.
2. Google Play policy risk exists for Accessibility-based blocking models.
3. Feature parity should be split into:
   - Parity A: server-backed product loops (accounts, sessions, streaks, social, rewards, subscriptions)
   - Parity B: strict app-level blocking/reporting behavior

## Recommended Delivery Phases

## Phase 0: Foundations (1-2 weeks)

- Finalize product/legal decision for Android blocking model.
- Freeze backend contract versions used by mobile.
- Create Android project skeleton and module layout.
- Add CI for lint, unit tests, and build verification.

Exit criteria:

- Architecture decision record (ADR) approved for blocking model.
- Android app boots with navigation shell and authenticated API call.

## Phase 1: Core Platform Layer (2-3 weeks)

- Implement auth, token storage, API client, error handling.
- Implement Room entities for core user/session/history models.
- Build app-wide state container and startup sync sequence.

Exit criteria:

- User can sign in and load profile/home data.
- Data survives process death and offline restart.

## Phase 2: Core Product Flows (3-5 weeks)

- Home, schedules, social groups, leaderboard, profile tabs.
- Present sessions create/start/end flow with local+remote sync.
- Goal, streak, rewards, and daily history rendering.

Exit criteria:

- Daily active user can complete full primary loop without iOS-only APIs.

## Phase 3: Blocking and Intention Systems (3-6 weeks)

- Implement Android intention model and open-limit logic.
- Implement selected blocking method (soft/hard hybrid recommended).
- Implement accountability triggers from intention break events.

Exit criteria:

- Intention setup, open tracking, streak logic, and partner messaging work reliably.

## Phase 4: Growth, Monetization, and Experimentation (2-3 weeks)

- Superwall placements and RevenueCat entitlement checks.
- Mixpanel/CustomerIO/Singular/Appstack/CAPI event parity.
- Experiments endpoint integration and exposure tracking parity.

Exit criteria:

- Subscription funnel is measurable and experimentable end-to-end.

## Phase 5: Android Surface Enhancements (2-4 weeks)

- Android widgets, app shortcuts, deep links, push channels.
- Foreground-service status UI replacing iOS live activity expectations.

Exit criteria:

- Android surfaces support quick access and session continuity.

## Phase 6: Stabilization and Rollout (2-3 weeks)

- Load test mobile-critical endpoints.
- Device matrix testing (OS versions, OEM variations, battery modes).
- Staged rollout with feature flags and kill switches.

Exit criteria:

- Crash-free/session success SLOs met for staged rollout.

## Suggested Workstreams

- Workstream A: App architecture + state + storage
- Workstream B: API + backend contract parity
- Workstream C: Sessions/intention/blocking behavior
- Workstream D: Monetization + analytics + experiments
- Workstream E: Android UX surfaces + release hardening

## Program Risks

- High: Android blocking policy or OEM restrictions reduce strict parity.
- Medium: Existing iOS data model has historical complexity and technical debt.
- Medium: Analytics event drift across SDKs causes attribution mismatch.
- Medium: Extension-to-app data sharing patterns need redesign for Android process model.

## Definition of Done (Program)

- Android app supports core daily behavior loops at production quality.
- Critical metrics are at or above iOS baseline for matched cohorts.
- Known platform-difference behavior is documented and accepted by product.
- Rollback path exists via remote config/feature flags.
