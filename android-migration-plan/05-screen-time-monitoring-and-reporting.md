# Android Migration Plan: Screen Time Monitoring and Reporting

## Scope
Migrate iOS screen-time monitoring/reporting behavior to Android-compatible implementations while documenting unavoidable platform differences.

## iOS Source of Truth

- Monitoring setup and thresholds: `swift/Screentox/Screentox/Features/ScreenTime/ScreenTimeMonitoringSetup.swift`
- Monitor extension lifecycle: `swift/Screentox/ScreentoxMonitorExtension/DeviceActivityMonitorExtension.swift`
- Report extension entry: `swift/Screentox/ScreentoxReportExtension/ScreentoxReportExtension.swift`
- In-app report state: `swift/Screentox/Screentox/Features/ScreenTime/ScreenTimeViewManager.swift`
- Apps report screen model: `swift/Screentox/Screentox/Screens/AppsReport/AppsReportScreenViewModel.swift`

## iOS Behavior Summary

- Continuous day interval monitoring with many time thresholds.
- Warning/goal notifications at threshold milestones.
- Daily report scheduling and streak impact calculations.
- Privacy-sandboxed DeviceActivity reporting extension.

## Android Reality

No public Android API matches iOS FamilyControls + DeviceActivity + ManagedSettings exact behavior for third-party consumer apps.

## Recommended Android Strategy

## Mode A: Reporting-first (policy-safe baseline)

- UsageStatsManager-based app/category usage aggregation.
- Daily threshold calculations in local worker.
- In-app report screens from local Room cache.

## Mode B: Behavior intervention (optional, policy-sensitive)

- AccessibilityService-based foreground app interception (if policy-approved).
- Soft intervention UI and timers rather than mandatory hard block where needed.

## Migration Tasks

1. Build usage ingestion service (periodic + on-demand refresh).
2. Implement threshold engine equivalent to iOS event table (30m to 16h milestones).
3. Persist daily screen time aggregates and threshold hit events.
4. Rebuild report UI screens with Compose charts/lists.
5. Add notification hooks for warning/goal events.

## Server Touchpoints

- Keep `screenTimeThresholds` and `dailyUserHistory` API compatibility.
- Ensure Android sends consistent payload types (date, timestamp, threshold type).

## Critical Product Decision

- Approve one of the parity definitions:
  - Strict parity target (hard interventions, policy risk)
  - Functional parity target (reporting + soft interventions)

## Risks

- OEM battery restrictions can reduce background reliability.
- Usage permission UX friction can reduce enabled-user rate.

## Mitigations

- Add guided permission onboarding with graceful degradation.
- Store last successful sync/refresh times and prompt user when stale.
- Add fallback manual refresh action on report screen.

## Definition of Done

- Daily screen time totals and threshold events are captured reliably for enabled users.
- Daily streak/goal logic receives required usage inputs.
- Users can access report insights equivalent to iOS report surfaces.
