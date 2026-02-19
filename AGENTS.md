# AGENTS.md

This file is the operational reference for Codex agents working in this repository.

## 1. Current truth (as of February 19, 2026)
- Planning/tech-debt artifacts were intentionally removed from this repo.
- Local database is active and used at runtime (Room + DAOs + Hilt wiring).
- Block enforcement is active (monitoring service + blocked activity + session/intention state).

## 2. Canonical docs to trust
- `README.md`
- `docs/features.md`
- `docs/permissions-and-enforcement.md`
- `docs/architecture.md`
- `docs/setup.md`

If behavior changes, update these docs in the same change.

## 3. Do not re-introduce removed planning docs
Do not recreate or reference:
- `planning/*`
- `android-migration-plan/*`
- `tech-debt`

If a user asks for roadmap/planning content, create new docs under `docs/` with current implementation context.

## 4. Core enforcement path
- Foreground detection: `app/src/main/java/com/bepresent/android/data/usage/UsageStatsRepository.kt`
- Runtime monitor: `app/src/main/java/com/bepresent/android/service/MonitoringService.kt`
- Shield UI entry: `app/src/main/java/com/bepresent/android/features/blocking/BlockedAppActivity.kt`
- Session orchestration: `app/src/main/java/com/bepresent/android/features/sessions/SessionManager.kt`
- Intention orchestration: `app/src/main/java/com/bepresent/android/features/intentions/IntentionManager.kt`

## 5. Permission model (must stay accurate in docs)
Critical gate (`PermissionManager.PermissionStatus.criticalGranted`) currently includes:
- Usage Access (`PACKAGE_USAGE_STATS`)
- Overlay (`SYSTEM_ALERT_WINDOW`)
- Accessibility service enablement

Important nuance:
- Active foreground detection path is UsageStats polling.
- `AccessibilityMonitorService` exists but event handling is currently reserved/no-op.

Any change to this model must update:
- `docs/features.md`
- `docs/permissions-and-enforcement.md`
- `app/src/main/java/com/bepresent/android/permissions/README.md`

## 6. Data layer status
Database is not blocked/inactive. It is active in runtime:
- `app/src/main/java/com/bepresent/android/data/db/BePresentDatabase.kt`
- `app/src/main/java/com/bepresent/android/di/AppModule.kt`
- session/intention managers and alarm receivers persist through DAOs

## 7. Safe edit workflow for agents
1. Confirm current behavior in code before editing docs.
2. Keep documentation aligned with implemented behavior, not aspirational behavior.
3. Avoid broad refactors unless requested.
4. Preserve existing package structure and Hilt wiring patterns.
5. If touching permissions/enforcement, validate manifest + onboarding + manager checks together.

## 8. Basic verification commands
Run from repo root:
- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest` (if tests exist/are relevant)

If you do not run validation, explicitly state that in your final update.
