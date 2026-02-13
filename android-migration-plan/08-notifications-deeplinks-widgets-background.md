# Android Migration Plan: Notifications, Deep Links, Widgets, and Background

## Scope
Migrate user re-engagement and system-entry surfaces from iOS notifications/extensions/widgets to Android equivalents.

## iOS Source of Truth

- Notification handling: `swift/Screentox/Screentox/Core/Notifications/NotificationManager.swift`
- App lifecycle, push token registration, deep-link routing: `swift/Screentox/Screentox/ScreentoxApp.swift`
- Quick actions: `swift/Screentox/Screentox/Core/QuickActions/QuickActionsManager.swift`
- Widgets/live activity:
  - `swift/Screentox/SessionWidget/SessionWidget.swift`
  - `swift/Screentox/StreakWidget/StreakWidget.swift`
  - `swift/Screentox/PresentWidgetExtension/PresentWidgetExtensionLiveActivity.swift`
- Server push/deep-link routes:
  - `server/routes/pushNotifications.ts`
  - `server/routes/universalLinks.ts`
  - `server/routes/messaging.ts`

## Android Equivalents

## Notifications

- FCM for remote push.
- Notification channels by category:
  - sessions
  - daily report
  - goal/awareness
  - social/accountability
- PendingIntent deep links into Compose routes.

## Deep Links

- Android App Links for `https` routes and custom scheme fallback.
- Router maps to:
  - join leaderboard/group
  - open specific tab/action
  - session widget entry points
  - paywall placement links

## Quick Actions

- Static and dynamic app shortcuts replacing iOS home screen quick actions.

## Widgets

- Android App Widgets (Glance preferred) for:
  - session status widget
  - streak widget
- Replace iOS live activity with foreground notification progress + lock screen widget where available.

## Background Work

- WorkManager for periodic report refresh and sync jobs.
- Foreground service only for active session timer/critical runtime.

## Migration Tasks

1. Implement FCM token registration endpoint payload update (`platform`, token type).
2. Build notification policy matrix and channel mapping.
3. Build deep-link router and test all known URL entry points.
4. Implement session + streak widgets with cached local data.
5. Implement quick actions and action telemetry.

## Risks

- Android background execution limits can suppress low-priority notifications.
- Widget refresh budgets differ by OEM/launcher.

## Mitigations

- Prefer explicit user-triggered refresh for stale widget states.
- Add notification delivery telemetry and retry strategy where meaningful.

## Definition of Done

- Push and local notifications route users to correct screens/actions.
- Widgets display accurate near-real-time session/streak states.
- Deep links and shortcuts are stable across cold start and warm start.
