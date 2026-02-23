package com.bepresent.android.ui.onboarding.v2.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/** Animation type for screen transitions. */
enum class ScreenAnimation {
    Intro,
    Drawer
}

/**
 * Timing curves and durations matched from iOS Transitions.swift.
 */
object OnboardingAnimSpecs {
    // Intro (standard screens)
    const val INTRO_IN_DURATION = 580
    const val INTRO_OUT_DURATION = 460
    val IntroEasing = CubicBezierEasing(0.3f, 0.0f, 0.2f, 1.0f)

    // Drawer (question screens)
    const val DRAWER_IN_DURATION = 570
    const val DRAWER_OUT_DURATION = 430
    val DrawerInEasing = CubicBezierEasing(0.8f, 0.0f, 0.5f, 1.05f)
    val DrawerOutEasing = CubicBezierEasing(0.5f, -0.2f, 0.6f, 1.1f)

    fun <T> introIn() = tween<T>(durationMillis = INTRO_IN_DURATION, easing = IntroEasing)
    fun <T> introOut() = tween<T>(durationMillis = INTRO_OUT_DURATION, easing = IntroEasing)
    fun <T> drawerIn() = tween<T>(durationMillis = DRAWER_IN_DURATION, easing = DrawerInEasing)
    fun <T> drawerOut() = tween<T>(durationMillis = DRAWER_OUT_DURATION, easing = DrawerOutEasing)
}
