package com.bepresent.android.ui.onboarding.v2.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bepresent.android.R
import com.bepresent.android.ui.onboarding.v2.OnboardingTokens
import com.bepresent.android.ui.onboarding.v2.OnboardingTypography

/**
 * Notification permission screen. On API 33+ requests POST_NOTIFICATIONS,
 * otherwise this is a no-op (the outer button just advances).
 *
 * The button that triggers this is the OnboardingContinueButton in OnboardingV2Screen
 * (buttonConfig == Full, buttonTitle == "Enable Notifications").
 * We augment it with the permission launcher here via a side-effect.
 */
@Composable
fun NotificationPermissionScreen() {
    // The actual permission request is triggered by the parent's Continue button.
    // On screens where buttonConfig == Full, the parent calls viewModel.advance().
    // We register the launcher here so it's composed, and trigger it from LaunchedEffect
    // when this screen first appears on API 33+.

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — either way we'll advance via the Continue button */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = OnboardingTokens.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.notifications_mask),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(200.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Stay on Track with\nNotifications",
            style = OnboardingTypography.h1,
            color = OnboardingTokens.NeutralBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Get reminders for your daily streak, session goals, and challenge progress.",
            style = OnboardingTypography.p2,
            color = OnboardingTokens.Neutral800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // The Continue button in the parent will trigger advance().
        // We request the permission here when the user taps it.
        // This is handled by the parent's onClick wiring; but we also
        // proactively request if on API 33+ when the screen loads.
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
