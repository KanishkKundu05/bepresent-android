# HomeV2 Screenshot Reference (Design Baseline)

This file maps the provided screenshot states to component docs for Kotlin/Compose implementation.

## Screenshot asset location
Place image files in:
- `swift-reference/screenshots/homev2/`

Canonical filenames used below:
- `image-01-inactive-home.png`
- `image-02-active-session-home.png`
- `image-03-session-success.png`
- `image-04-inactive-home-carousel-checked.png`

## Image #1 - Inactive state home page
![Image #1 Inactive Home](./screenshots/homev2/image-01-inactive-home.png)

### Use this to implement
- Root layout + background: `swift-reference/HOMEV2_PORTING/01_HOME_ROOT.md`
- Header: `swift-reference/HOMEV2_PORTING/02_HEADER.md`
- Date carousel: `swift-reference/HOMEV2_PORTING/03_DATE_CAROUSEL.md`
- Idle card shell + timer + mode chips + CTA: `swift-reference/HOMEV2_PORTING/04_BLOCKED_TIME_IDLE_CARD.md`
- Shared tokens/primitives: `swift-reference/HOMEV2_PORTING/10_SHARED_PRIMITIVES_AND_TOKENS.md`
- Bottom tabs (visual parity): `swift-reference/HOMEV2_PORTING/09_TAB_BAR.md`

## Image #2 - Active state home page
![Image #2 Active Session Home](./screenshots/homev2/image-02-active-session-home.png)

### Use this to implement
- Active card and subcomponents (translucent allow-list chip, timer capsule, give-up CTA): `swift-reference/HOMEV2_PORTING/06_ACTIVE_SESSION_CARD.md`
- Root state switching: `swift-reference/HOMEV2_PORTING/01_HOME_ROOT.md`
- Shared tokens/primitives: `swift-reference/HOMEV2_PORTING/10_SHARED_PRIMITIVES_AND_TOKENS.md`
- Bottom tabs (visual parity): `swift-reference/HOMEV2_PORTING/09_TAB_BAR.md`

## Image #3 - Session success page
![Image #3 Session Success](./screenshots/homev2/image-03-session-success.png)

### Use this to implement
- Completion -> Claim XP flow context: `swift-reference/HOMEV2_PORTING.md` (Session completion + Claim XP sections)
- Home state wiring that triggers completion flow: `swift-reference/HOMEV2_PORTING/01_HOME_ROOT.md`
- Active-session completion trigger context: `swift-reference/HOMEV2_PORTING/06_ACTIVE_SESSION_CARD.md`

## Image #4 - Inactive with calendar checked state
![Image #4 Inactive Home Calendar Checked](./screenshots/homev2/image-04-inactive-home-carousel-checked.png)

### Use this to implement
- Date carousel checked/current day variants: `swift-reference/HOMEV2_PORTING/03_DATE_CAROUSEL.md`
- Idle blocked-time card with record chip/non-zero timer state: `swift-reference/HOMEV2_PORTING/04_BLOCKED_TIME_IDLE_CARD.md`

## Implementation order for current phase
1. Match Image #1 first (inactive baseline).
2. Apply Image #4 deltas (checked day + daily record state).
3. Prepare architecture for Image #2 and #3 without full behavior if phase-limited.
