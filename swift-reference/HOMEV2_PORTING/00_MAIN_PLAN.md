# HomeV2 Kotlin Rebuild Main Plan

## Goal
Use iOS HomeV2 as the source of truth and rebuild matching UI/behavior in Kotlin + Jetpack Compose.

## How to use this doc set
1. Implement shared primitives and tokens first.
2. Build root layout and state switching.
3. Build each major card/state component.
4. Integrate bottom tabs and navigation flows.
5. Validate against screenshot checklist and iOS behavior.

## Document map
- `swift-reference/HOMEV2_SCREENSHOTS.md`
- `swift-reference/HOMEV2_PORTING/01_HOME_ROOT.md`
- `swift-reference/HOMEV2_PORTING/02_HEADER.md`
- `swift-reference/HOMEV2_PORTING/03_DATE_CAROUSEL.md`
- `swift-reference/HOMEV2_PORTING/04_BLOCKED_TIME_IDLE_CARD.md`
- `swift-reference/HOMEV2_PORTING/04A_SESSION_MODE_SHEET.md`
- `swift-reference/HOMEV2_PORTING/04B_SESSION_GOAL_SHEET.md`
- `swift-reference/HOMEV2_PORTING/05_COUNTDOWN_STATE.md`
- `swift-reference/HOMEV2_PORTING/06_ACTIVE_SESSION_CARD.md`
- `swift-reference/HOMEV2_PORTING/07_INTENTIONS_CARD.md`
- `swift-reference/HOMEV2_PORTING/08_DAILY_QUEST_CARD.md`
- `swift-reference/HOMEV2_PORTING/09_TAB_BAR.md`
- `swift-reference/HOMEV2_PORTING/10_SHARED_PRIMITIVES_AND_TOKENS.md`

## Kotlin implementation order (recommended)
1. `HomeV2Tokens` + shared components (`FullButton`, segmented picker, progress bar, card/background wrappers).
2. `HomeV2Screen` root and state model (`idle`, `countdown`, `active`).
3. `HomeHeaderRow`.
4. `HomeDateCarousel`.
5. `BlockedTimeCard` + both bottom sheets.
6. `SessionCountdownCard`.
7. `ActiveSessionCard` (+ break variant and complete variant).
8. `IntentionsCard`.
9. `DailyQuestCard`.
10. Bottom nav integration.

## Acceptance checklist
- Visual hierarchy matches iOS.
- Card shapes, spacing rhythm, and gradients match.
- All state transitions match trigger logic.
- Tab order and tab icon mapping are identical.

