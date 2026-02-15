# Features

## App Intentions

Per-app daily open limits with a shield/intervention screen.

### Flow
1. User taps "+ Add" on dashboard → app picker (single-select)
2. Configures: allowed opens per day (1-10), time per open (1-30 min)
3. App is immediately shielded
4. When user opens the blocked app:
   - Shield shows with opens used/allowed and streak count
   - "Nevermind" → goes home
   - "Open [App]" → starts timed window, app unblocked for N minutes
5. Timer expires → app re-blocked, shield re-appears if still in foreground

### Over-Limit Behavior
When all daily opens are used:
- Shield shows stronger messaging ("You've used all 3 opens today")
- "Open Anyway" is still available (soft enforcement)
- Opening past the limit breaks the streak at midnight (unless freeze active)

### Streaks
- Incremented daily at midnight if opens stayed within limit
- Reset to 0 if over limit (unless streak freeze consumed)
- Streak freeze: 1 per week, granted every Monday, protects all intentions

### Editing/Deleting
Tap an intention card on the dashboard to edit limits or delete.

---

## Blocking Sessions

Timed focus sessions that block selected apps.

### Configuration
- Session name (optional, defaults to "Focus Session")
- Goal duration: 5, 10, 15, 20, 30, 45, 60, 90, or 120 minutes
- Apps to block (multi-select from app picker)
- Beast Mode toggle (disables "Give Up")

### State Machine
```
idle → active → goalReached → completed (+XP)
         ↓
       gaveUp (0 XP)
         ↓
       canceled (only within first 10s, 0 XP)
```

### XP/Coins Rewards
| Duration | XP | Coins |
|---|---|---|
| ≤ 15 min | 3 | 3 |
| ≤ 30 min | 5 | 5 |
| ≤ 45 min | 8 | 8 |
| ≤ 60 min | 10 | 10 |
| ≤ 90 min | 15 | 15 |
| ≤ 120 min | 25 | 25 |

### Session + Intention Priority
If both apply to the same app, the session shield takes priority. The intention's open count is not affected by session blocks.

---

## Shield Screen Variants

### Session Active
- Shows shield icon and session name
- "Be Present" → navigates home
- "Unlock?" → explains how to end session (disabled in beast mode)

### Goal Reached
- Shows celebration and XP preview
- "Complete" → ends session, awards XP
- "Stay Present" → goes home, session continues

### App Intention
- Shows app name, opens used/allowed, streak
- "Nevermind" → goes home
- "Open [App] (for N minutes)" → starts timed window

### Intention Over-Limit
- Same as intention but with warning about streak breaking
- "Open Anyway" replaces the open button

---

## Dashboard

Single scrollable screen with:
1. **Header**: App name, streak count, XP total
2. **Screen Time Card**: Circular progress (8h max), per-app usage chips
3. **Active Session Card** (if session running): Timer, give up/complete actions
4. **Intentions Row**: Horizontal scroll of intention cards with "+ Add"
5. **Start Session CTA**: Opens session configuration sheet
6. **Permission Banner** (if critical permission missing): Tap to fix
