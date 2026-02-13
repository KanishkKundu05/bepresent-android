# Android Migration Plan: Subscriptions, Analytics, Experiments, Testing, and Release

## Scope
Migrate monetization, growth telemetry, experimentation, and production rollout controls to Android.

## iOS Source of Truth

- Subscription and paywall orchestration:
  - `swift/Screentox/Screentox/Core/Subscriptions/SuperwallManager.swift`
  - `swift/Screentox/Screentox/Core/Subscriptions/EntitlementManager.swift`
  - `swift/Screentox/Screentox/Core/Subscriptions/PurchaseManager.swift`
- Analytics manager: `swift/Screentox/Screentox/Core/Analytics/AnalyticsManager.swift`
- Experiment client: `swift/Screentox/Screentox/Features/FeatureToggles/ExperimentManager.swift`
- Experiment model: `swift/Screentox/Screentox/Features/FeatureToggles/Models/Feature.swift`
- Backend routes:
  - `server/routes/subscriptions.ts`
  - `server/routes/experiments.ts`
  - `server/routes/capi.ts`

## Subscriptions and Paywalls

## Android Plan

- Keep Superwall placements aligned with iOS placement names.
- Keep RevenueCat entitlement source as canonical for `hasPro` behavior.
- Add Android product IDs and mapping layer per experiment group.
- Mirror restore purchase and cancellation status logic in profile/home banners.

## Analytics and Attribution

SDK parity targets:

- Mixpanel
- CustomerIO
- Singular
- Appstack/Facebook CAPI bridging

Requirements:

- Preserve event names and property contracts where possible.
- Maintain event-id correlation for purchase and trial events.
- Define Android-specific supplemental properties (`platform`, `app_build`, `device_class`).

## Experiments

- Reuse existing `/experiments` endpoint and assignment model.
- Port experiment exposure semantics (`$experiment_started`) to Android.
- Support local override tooling for QA builds.

## Testing Strategy

## Automated

- Unit tests: streak logic, intention logic, session state machine, DTO mapping.
- Integration tests: repository + mock network + Room migrations.
- UI tests: onboarding/auth/session/social critical flows.

## Manual Regression Matrix

- Android OS versions (current major + previous 2)
- OEM coverage (Pixel + Samsung + one aggressive battery OEM)
- Connectivity conditions (offline/slow/packet loss)
- Notification and deep-link cold start behavior

## Release and Rollout

1. Internal QA track.
2. Closed testing track with staff/power users.
3. Staged production rollout (5%, 20%, 50%, 100%).
4. Feature flag guards for high-risk surfaces (blocking/intention enforcement).

## Operational Metrics to Gate Rollout

- Crash-free users
- ANR rate
- Auth success rate
- Session start->complete conversion
- Intention open-close success ratio
- Push delivery/open rates
- Subscription funnel conversion

## Risks

- Attribution drift between SDKs and backend CAPI events.
- SKU and entitlement mismatches between iOS and Android setups.

## Mitigations

- Add event validation dashboard before public rollout.
- Add subscription smoke tests per SKU and entitlement edge case.

## Definition of Done

- Monetization funnel and analytics stack are production reliable.
- Experiment assignments/exposure events are valid and auditable.
- Android release can be safely ramped with observable guardrails.
