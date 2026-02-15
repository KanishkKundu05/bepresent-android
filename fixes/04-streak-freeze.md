# Fix 04: Streak Freeze Must Protect All Over-Limit Intentions

**Resolves:** TD-024, TD-025

## Problem 1: Freeze Only Protects First Over-Limit Intention (TD-024)

The `DailyResetWorker` loop uses a `freezeUsed` boolean that flips after the first over-limit intention consumes the freeze. Any subsequent over-limit intentions in the same loop iteration get their streak reset to 0:

```kotlin
// DailyResetWorker.kt:33-49
val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()
var freezeUsed = false

for (intention in intentions) {
    if (intention.lastResetDate == today) continue

    val withinLimit = intention.totalOpensToday <= intention.allowedOpensPerDay
    val newStreak: Int

    if (withinLimit) {
        newStreak = intention.streak + 1
    } else if (freezeAvailable && !freezeUsed) {
        newStreak = intention.streak + 1
        freezeUsed = true              // <-- gate closes after first use
    } else {
        newStreak = 0
    }
    // ...
}
```

**Spec says:** A streak freeze protects the user's streaks for the entire day, not just one intention.

## Problem 2: Monday Grant Can Re-grant a Just-Consumed Freeze (TD-025)

The Monday auto-grant logic runs **after** the freeze-consumption loop, in the same `doWork()` invocation:

```kotlin
// DailyResetWorker.kt:62-75
if (freezeUsed) {
    preferencesManager.setStreakFreezeAvailable(false)
}

// Grant new freeze on Mondays
val dayOfWeek = LocalDate.now().dayOfWeek
if (dayOfWeek == DayOfWeek.MONDAY) {
    val lastGrantDate = preferencesManager.getLastFreezeGrantDateOnce()
    if (lastGrantDate != today) {
        preferencesManager.setStreakFreezeAvailable(true)   // <-- re-grants what was just consumed
        preferencesManager.setLastFreezeGrantDate(today)
    }
}
```

If the worker runs on a Monday and the freeze was consumed, the freeze gets set to `false` and then immediately back to `true`. The user effectively never loses a freeze on Mondays.

## Root Cause

1. The `freezeUsed` flag was designed as a per-intention gate instead of a per-day flag.
2. The Monday grant was placed after consumption without considering the interaction.

## Fix

### Change 1: Apply freeze to all over-limit intentions (TD-024)

Decouple the decision "should we use a freeze today?" from the per-intention loop. Pre-scan to determine if any intention needs a freeze, then apply it uniformly.

#### Before

```kotlin
val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()
var freezeUsed = false

for (intention in intentions) {
    if (intention.lastResetDate == today) continue

    val withinLimit = intention.totalOpensToday <= intention.allowedOpensPerDay
    val newStreak: Int

    if (withinLimit) {
        newStreak = intention.streak + 1
    } else if (freezeAvailable && !freezeUsed) {
        newStreak = intention.streak + 1
        freezeUsed = true
    } else {
        newStreak = 0
    }

    val updated = intention.copy(
        streak = newStreak,
        totalOpensToday = 0,
        lastResetDate = today,
        currentlyOpen = false,
        openedAt = null
    )
    intentionDao.upsert(updated)
}
```

#### After

```kotlin
val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()

// Pre-scan: does any intention need a freeze today?
val pendingIntentions = intentions.filter { it.lastResetDate != today }
val anyOverLimit = pendingIntentions.any { it.totalOpensToday > it.allowedOpensPerDay }
val freezeActive = freezeAvailable && anyOverLimit

for (intention in pendingIntentions) {
    val withinLimit = intention.totalOpensToday <= intention.allowedOpensPerDay
    val newStreak: Int = if (withinLimit || freezeActive) {
        intention.streak + 1
    } else {
        0
    }

    val updated = intention.copy(
        streak = newStreak,
        totalOpensToday = 0,
        lastResetDate = today,
        currentlyOpen = false,
        openedAt = null
    )
    intentionDao.upsert(updated)
}
```

Key differences:
- `freezeActive` is a single boolean decided **before** the loop — applies to all intentions equally.
- If any intention is over-limit and a freeze is available, the freeze protects **every** over-limit intention that day.
- If no intention is over-limit, the freeze is not consumed.

### Change 2: Move Monday grant before consumption check (TD-025)

#### Before (order)

```kotlin
// 1. Consume freeze
if (freezeUsed) {
    preferencesManager.setStreakFreezeAvailable(false)
}

// 2. Grant new freeze on Mondays
if (dayOfWeek == DayOfWeek.MONDAY) { ... }
```

#### After (order)

```kotlin
// 1. Grant new freeze on Mondays FIRST
val dayOfWeek = LocalDate.now().dayOfWeek
if (dayOfWeek == DayOfWeek.MONDAY) {
    val lastGrantDate = preferencesManager.getLastFreezeGrantDateOnce()
    if (lastGrantDate != today) {
        preferencesManager.setStreakFreezeAvailable(true)
        preferencesManager.setLastFreezeGrantDate(today)
    }
}

// 2. THEN run the reset loop (which reads freeze state and may consume it)
val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()
// ... pre-scan + loop from Change 1 ...

// 3. Consume freeze if it was activated
if (freezeActive) {
    preferencesManager.setStreakFreezeAvailable(false)
}
```

This way, a Monday grant happens first, and if the user goes over limit that day, the newly granted freeze is consumed — producing the correct net result (freeze used, not available until next Monday).

### Full `doWork()` after both changes

```kotlin
override suspend fun doWork(): Result {
    val today = LocalDate.now().toString()
    val intentions = intentionDao.getAllOnce()

    // Step 1: Grant new freeze on Mondays (before consumption)
    val dayOfWeek = LocalDate.now().dayOfWeek
    if (dayOfWeek == DayOfWeek.MONDAY) {
        val lastGrantDate = preferencesManager.getLastFreezeGrantDateOnce()
        if (lastGrantDate != today) {
            preferencesManager.setStreakFreezeAvailable(true)
            preferencesManager.setLastFreezeGrantDate(today)
        }
    }

    // Step 2: Determine freeze state
    val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()
    val pendingIntentions = intentions.filter { it.lastResetDate != today }
    val anyOverLimit = pendingIntentions.any { it.totalOpensToday > it.allowedOpensPerDay }
    val freezeActive = freezeAvailable && anyOverLimit

    // Step 3: Reset all intentions
    for (intention in pendingIntentions) {
        val withinLimit = intention.totalOpensToday <= intention.allowedOpensPerDay
        val newStreak: Int = if (withinLimit || freezeActive) {
            intention.streak + 1
        } else {
            0
        }

        val updated = intention.copy(
            streak = newStreak,
            totalOpensToday = 0,
            lastResetDate = today,
            currentlyOpen = false,
            openedAt = null
        )
        intentionDao.upsert(updated)
    }

    // Step 4: Consume freeze if used
    if (freezeActive) {
        preferencesManager.setStreakFreezeAvailable(false)
    }

    return Result.success()
}
```

## Files Touched

| File | Change |
|------|--------|
| `DailyResetWorker.kt` | Rewrite `doWork()` — pre-scan freeze logic, reorder Monday grant |

## Verification

1. **Multi-intention freeze test:** Set up 3 intentions, go over limit on 2 of them, have a freeze available. Run daily reset. **Expected:** All 3 streaks increment (2 protected by freeze, 1 within limit). Freeze is consumed.
2. **No over-limit, freeze not consumed:** All intentions within limit, freeze available. **Expected:** All streaks increment, freeze remains available.
3. **Monday grant + consumption:** On a Monday, have a freeze available (just granted), go over limit. **Expected:** Freeze is granted, then consumed in same run. `freezeAvailable` ends as `false`.
4. **Monday grant, no consumption:** On a Monday, all within limit. **Expected:** Freeze is granted and remains available.
5. **No freeze available, over limit:** Go over limit with no freeze. **Expected:** Over-limit intentions reset to streak 0, within-limit intentions increment.
