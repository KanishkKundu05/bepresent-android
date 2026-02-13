# Android Migration Plan: Data Storage, Sync, and Caching

## Scope
Migrate local persistence and sync orchestration from CoreData + shared defaults patterns to Room + DataStore on Android.

## iOS Source of Truth

- CoreData controller and sync logic: `swift/Screentox/Screentox/Core/Storage/DataController.swift`
- Shared defaults models/helpers: `swift/Screentox/Screentox/Core/Storage/Defaults.swift`
- Constants and keys: `swift/Screentox/Screentox/Core/Util/Constants.swift`
- User state persistence: `swift/Screentox/Screentox/Core/Models/UserState.swift`
- Backend schema references: `server/db/schema.ts`

## Current Storage Pattern

- CoreData for historical entities (history, sessions, actions, thresholds, coins-related state).
- UserDefaults/App Group for cross-target state and fast reads.
- Hybrid sync model where local changes are periodically pushed/pulled.

## Target Android Storage Pattern

- Room database for all structured entities:
  - daily history
  - present sessions
  - session actions
  - goals
  - rewards cache
  - coins cache
- DataStore (Proto preferred) for lightweight config/state:
  - onboarding flags
  - selected tab/filter
  - session runtime flags
  - experiments cache
- EncryptedSharedPreferences for secrets only.

## Sync Engine Design

- Create `SyncCoordinator` with independent jobs:
  - score/history sync
  - goal sync
  - present sessions sync
  - present session actions sync
  - user state sync
  - rewards sync
- Use WorkManager for periodic or opportunistic sync.
- Use conflict strategy:
  - last-updated timestamps for mutable scalar objects
  - append-only with dedupe keys for event records

## Migration Tasks

1. Define Room entities and DAO interfaces mapped from iOS domain models.
2. Implement repository-level mappers from API DTOs to Room entities.
3. Port timestamp-based reconciliation logic from `DataController`.
4. Build startup hydration and background refresh routines.
5. Add migration scripts for schema upgrades.

## Data Integrity Requirements

- Sync calls must tolerate partial failure without corrupting local state.
- User switch must cleanly partition user-scoped data.
- No UI reads directly from network response without cache persistence for critical flows.

## Risks

- Existing iOS logic includes legacy/deprecated paths; blind port can copy debt.
- Multiple timestamp formats can break ordering and dedupe.

## Mitigations

- Normalize all persisted time fields to UTC epoch millis in Room.
- Add repository contract tests for every sync operation.

## Definition of Done

- Offline open is functional for core screens.
- Sync converges local and remote state after reconnect.
- User-scoped data isolation is verified by automated tests.
