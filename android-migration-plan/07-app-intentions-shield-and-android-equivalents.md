# Android Migration Plan: App Intentions, Shield Flow, and Equivalents

## Scope
Migrate App Intention (open limits, streak/freeze logic, shield interactions) to Android capabilities.

## iOS Source of Truth

- Intention manager: `swift/Screentox/Screentox/Features/AppIntentions/V2/AppLimitManagerV2.swift`
- Intention model/protocols:
  - `swift/Screentox/Screentox/Features/AppIntentions/V2/AppIntentionV2.swift`
  - `swift/Screentox/Screentox/Features/AppIntentions/V2/IntentionV2+Protocols.swift`
- Backend sync: `swift/Screentox/Screentox/Features/AppIntentions/V2/AppLimitManagerV2+Sync.swift`
- Shield action behavior: `swift/Screentox/PresentShieldActionExtension/ShieldActionExtension.swift`
- Shield configuration UI states: `swift/Screentox/PresentShieldConfigurationExtension/ShieldConfigurationExtension.swift`
- Backend endpoints: `server/routes/appIntentions.ts`

## iOS Behavior Summary

- Per-app daily open limits with time-per-open windows.
- Streak progression and streak break detection.
- Freeze mechanics and weekly freeze replenishment.
- Shield UI with primary/secondary actions and contextual messaging.

## Android Equivalent Architecture

## Domain Layer

- Keep `AppIntention` model shape close to iOS for backend parity.
- Reuse no-time-date logic in Kotlin for streak calculations.

## Enforcement Layer

- Soft enforcement: intervention screen/overlay + timer windows.
- Optional hard enforcement: AccessibilityService-driven interception (policy dependent).

## UI Layer

- Replace shield extension screens with in-app Compose dialogs/activities and heads-up notifications.
- Preserve user decision flow:
  - stay focused
  - open temporarily
  - freeze prompt

## Migration Tasks

1. Port intention model and streak/freeze calculations.
2. Implement local storage and backend sync of intentions.
3. Implement open-window logic with close-notification scheduling.
4. Implement fallback intention behavior for unknown/changed app identity.
5. Implement accountability message trigger when streak breaks.

## Policy and Compliance Guardrails

- Maintain a documented fallback product mode if blocking permissions are denied.
- Keep intervention UX transparent and user-controllable.

## Risks

- Android app identifiers/package visibility constraints can affect app-picking UX.
- Inconsistent app foreground detection across OEM variants.

## Mitigations

- Build abstraction over app-identification signals.
- Add debug telemetry for open detection and missed close events.

## Definition of Done

- Intention lifecycle (create/update/open/break/freeze/remove) works with reliable local state and backend sync.
- Users receive equivalent motivation and accountability loops even when strict block is unavailable.
