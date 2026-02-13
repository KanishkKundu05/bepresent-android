# Android Migration Plan: Networking and Backend Contracts

## Scope
Migrate iOS API client behavior to a typed Android network layer with contract safety and test coverage.

## iOS Source of Truth

- Legacy API client: `swift/Screentox/Screentox/Core/Networking/Network.swift`
- Typed route helpers: `swift/Screentox/Screentox/Core/Networking/New/Routes.swift`
- Shared request wrapper: `swift/Screentox/Screentox/Core/Networking/New/NetworkingService.swift`
- Backend registration: `server/app.ts`, `server/routes/*`

## Key Existing Endpoint Domains

- Accounts/auth: `/createAccount/*`, `/login`, `/accounts/*`
- Score/history: `/score/*`, `/dailyUserHistory/*`, `/goal/*`
- Social/leaderboards: `/leaderboards/*`, `/groupLeaderboards/*`
- Sessions: `/presentSessions/*`
- Intentions: `/appIntentions`
- Notifications/messaging: `/pushNotifications/*`, `/messaging/*`
- Monetization: `/subscriptions/*`, `/stripe/*`, `/coins/*`, `/rewards/*`
- Experimentation/growth: `/experiments`, `/capi/*`

## Target Android Networking Layer

- Retrofit service interfaces grouped by domain.
- OkHttp interceptors:
  - auth header
  - request-id propagation
  - retry/backoff policy for idempotent calls
- Unified response envelope mapping (success/error).
- Domain repositories convert DTOs into app models.

## Migration Tasks

1. Generate Kotlin DTOs from current server responses used by Android MVP scope.
2. Implement auth interceptor mirroring bearer-token behavior from iOS.
3. Build repository layer for:
   - Auth
   - Sessions
   - Leaderboards/groups
   - Daily history/goal/streak
   - Rewards/coins
   - Experiments
4. Add network test suite:
   - serialization tests
   - status code mapping tests
   - contract tests against local/staging backend
5. Add endpoint ownership matrix and deprecate unused legacy calls in Android client.

## Compatibility and Hardening

- Keep backward compatibility for current iOS responses while Android ships.
- Add explicit `platform` field where server behavior diverges (example: device token registration for APNs vs FCM).
- Timeouts and retry strategy should reflect call criticality.

## Risks

- `Network.swift` has inconsistent styles and partial legacy paths.
- Different date formats across endpoints can create parse failures.

## Mitigations

- Centralize date parsing in one serializer strategy.
- Start with read-only endpoints, then mutate endpoints with idempotency checks.
- Add high-signal logging with request ID and endpoint tags.

## Definition of Done

- Android client can execute full auth -> home bootstrap -> session create/start/end -> social update flows.
- Error handling is deterministic and surfaces user-relevant messages.
- API contract tests pass in CI.
