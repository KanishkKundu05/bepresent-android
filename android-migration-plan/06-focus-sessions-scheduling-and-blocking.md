# Android Migration Plan: Focus Sessions, Scheduling, and Blocking

## Scope
Migrate Present Session runtime behavior, scheduling, and session analytics/sync to Android.

## iOS Source of Truth

- Session manager: `swift/Screentox/Screentox/Features/Sessions/PresentSessionManager.swift`
- Shared runtime/session state: `swift/Screentox/ScreentoxMonitorExtension/Managers/SharedPresentSessionManager.swift`
- Scheduled session view model: `swift/Screentox/Screentox/Screens/Schedules/ScheduledSessionsV2ViewModel.swift`
- Session UI + interactions: `swift/Screentox/Screentox/Features/Sessions/PresentSessionView.swift`
- Session data APIs: `server/routes/presentSessions.ts`

## iOS Behavior Summary

- Create/start/end sessions with points/coins and action logs.
- Support scheduled recurring sessions by weekday + start/end.
- Sync sessions and actions to backend.
- Trigger notifications and shielding during active session.

## Android Target Design

## Core Domain Objects

- `PresentSession`
- `PresentSessionAction`
- `ScheduledPresentSession`
- `SessionRuntimeState`

## Runtime Components

- `SessionCoordinator` (domain orchestration)
- `SessionScheduler` (WorkManager/AlarmManager triggers)
- `SessionForegroundService` (ongoing timer/notification while active)
- `SessionSyncWorker` (push/pull session and action updates)

## Migration Tasks

1. Port local model and persistence for sessions/actions/schedules.
2. Implement session create/start/end state machine.
3. Implement scheduled session triggers with timezone-safe logic.
4. Implement points/coins grants and action event logging.
5. Implement backend push for schedules (`pushScheduledSessions`) and actions.

## Blocking Integration

- Session runtime integrates with selected Android blocking strategy.
- If strict blocking unavailable, keep session timer and accountability loops functional.

## Data Consistency

- Ensure action sync ordering to avoid foreign key issues (session before action) similar to iOS behavior.
- Preserve session IDs for group/session invite flows.

## Risks

- Exact trigger timing may vary across OEMs under battery optimization.
- Scheduled sessions crossing midnight need careful handling.

## Mitigations

- Use durable scheduling with recovery on boot/app launch.
- Add idempotent action inserts on server side where needed.

## Definition of Done

- User can run solo and scheduled sessions end-to-end.
- Session actions, XP/coins, and server synchronization are consistent and resilient.
