# Android Migration Plan: Architecture, Navigation, and State

## Scope
Migrate app shell, global state patterns, and navigation from SwiftUI singleton/environment-object patterns to Android Compose architecture.

## iOS Source of Truth

- App lifecycle and boot sync: `swift/Screentox/Screentox/ScreentoxApp.swift`
- App-wide mutable state: `swift/Screentox/Screentox/Core/Models/AppState.swift`
- Main tabs: `swift/Screentox/Screentox/Screens/TabContainer/TabContainerView.swift`
- Tab state bindings: `swift/Screentox/Screentox/Screens/TabContainer/TabContainerViewModel.swift`
- Home state behavior: `swift/Screentox/Screentox/Screens/HomeV2/HomeV2ViewModel.swift`

## Current Pattern to Replace

- Multiple singletons (`AppState`, `UserState`, `PresentSessionManager`, etc.)
- Side-effect-heavy app startup task groups in app entry
- `@Published` and `@AppStorage` mixed mutation across many layers
- State updates from extensions via shared defaults observers

## Target Android Architecture

## Modules

- `app`: navigation shell and app startup
- `core:ui`: theme, components
- `core:model`: domain models
- `core:data`: repositories, API, persistence
- `feature:*`: home, schedules, social, leaderboard, profile, sessions, intentions

## State Model

- Replace singleton mutable objects with:
  - repository interfaces
  - scoped ViewModels
  - one app-level `SessionState` stream for auth/user bootstrap
- Replace `@AppStorage` behavior with DataStore-backed settings repository.

## Navigation Model

- Use Navigation Compose with bottom tabs:
  - Home
  - Schedules
  - Leaderboard
  - Screen Time
  - Social
- Preserve deep-link routes currently supported by `ScreentoxApp.handleUrl(...)`.

## Startup Sequence (Android)

1. Read secure credentials.
2. Resolve signed-in state.
3. Run parallel bootstrap calls (user state, rewards, leaderboard rank, experiments).
4. Hydrate local cache.
5. Emit UI state for tab shell.

## Task Breakdown

1. Create architecture package layout and base gradle modules.
2. Implement `AppStartupCoordinator` to mirror `setAppState()` behavior.
3. Build `AppStateRepository` + immutable UI state snapshots.
4. Build bottom-tab graph with typed routes.
5. Add process death restoration tests for nav and key state.

## Risks

- Over-centralized state object on Android can recreate iOS coupling.
- Recomposition loops if events and state are not separated.

## Mitigations

- Use clear state/event contracts per feature.
- Keep one-way data flow with explicit reducers where practical.
- Add lint checks for forbidden singleton mutable state in feature modules.

## Definition of Done

- App launches into Compose shell with authenticated/non-authenticated flows.
- Tab navigation, deep links, and top-level startup sync execute reliably.
- No global mutable singleton is required for feature-level screen behavior.
