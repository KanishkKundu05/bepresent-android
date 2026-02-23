package com.bepresent.android.ui.onboarding.v2.util

/**
 * Calculates approximate years spent on phone based on daily screen time hours
 * and user's age bracket. Matches iOS ShockPageViewModel logic.
 */
fun calculateYearsOnPhone(dailyHours: Float, ageRange: String): Int {
    val yearsWithSmartphone = when (ageRange) {
        "Under 18" -> 5
        "18-24" -> 8
        "25-34" -> 12
        "35-44" -> 10
        "45-54" -> 8
        "55-64" -> 6
        "65+" -> 4
        else -> 8
    }
    val hoursPerYear = dailyHours * 365f
    val totalHours = hoursPerYear * yearsWithSmartphone
    val years = (totalHours / 8760f).toInt() // 8760 hours per year
    return years.coerceAtLeast(1)
}

/** Maps screen time answer to approximate daily hours. */
fun screenTimeAnswerToHours(answer: String): Float = when {
    answer.contains("Less than 2") -> 1.5f
    answer.contains("2-3") -> 2.5f
    answer.contains("3-4") -> 3.5f
    answer.contains("4-5") -> 4.5f
    answer.contains("5-6") -> 5.5f
    answer.contains("6-8") -> 7f
    answer.contains("8-10") -> 9f
    answer.contains("10+") -> 11f
    else -> 5f
}

/** Returns the "years back" value (half of years on phone, minimum 1). */
fun calculateYearsBack(yearsOnPhone: Int): Int = (yearsOnPhone / 2).coerceAtLeast(1)
