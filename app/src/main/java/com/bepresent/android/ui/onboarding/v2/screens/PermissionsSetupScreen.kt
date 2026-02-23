package com.bepresent.android.ui.onboarding.v2.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bepresent.android.permissions.PermissionManager
import com.bepresent.android.ui.onboarding.OnboardingEntryPoint
import com.bepresent.android.ui.onboarding.v2.OnboardingTokens
import com.bepresent.android.ui.onboarding.v2.OnboardingTypography
import com.bepresent.android.ui.onboarding.v2.components.OnboardingContinueButton
import com.bepresent.android.ui.onboarding.v2.components.OnboardingButtonAppearance
import dagger.hilt.android.EntryPointAccessors

private const val STEP_OVERLAY = 0
private const val STEP_USAGE = 1
private const val STEP_ACCESSIBILITY = 2
private const val STEP_DONE = 3

@Composable
fun PermissionsSetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            OnboardingEntryPoint::class.java
        )
    }
    val permissionManager = remember { entryPoint.permissionManager() }

    var overlayGranted by remember { mutableStateOf(permissionManager.hasOverlayPermission()) }
    var usageGranted by remember { mutableStateOf(permissionManager.hasUsageStatsPermission()) }
    var accessibilityGranted by remember { mutableStateOf(permissionManager.hasAccessibilityPermission()) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    var currentStep by remember {
        mutableIntStateOf(nextStep(overlayGranted, usageGranted, accessibilityGranted))
    }

    // Re-check on resume
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            overlayGranted = permissionManager.hasOverlayPermission()
            usageGranted = permissionManager.hasUsageStatsPermission()
            accessibilityGranted = permissionManager.hasAccessibilityPermission()
            currentStep = nextStep(overlayGranted, usageGranted, accessibilityGranted)
        }
    }

    // Auto-advance when all done
    LaunchedEffect(currentStep) {
        if (currentStep == STEP_DONE) {
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = OnboardingTokens.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = currentStep.coerceIn(STEP_OVERLAY, STEP_ACCESSIBILITY),
            label = "permissions"
        ) { step ->
            when (step) {
                STEP_OVERLAY -> PermissionStepContent(
                    title = "Enable BePresent to block distracting apps",
                    subtitle = "Don't worry, you can take a break any time.",
                    step = "1 of 3",
                    buttonTitle = "Allow Display Over Apps",
                    onContinue = {
                        if (overlayGranted) {
                            currentStep = nextStep(overlayGranted, usageGranted, accessibilityGranted)
                        } else {
                            openSettings(
                                context,
                                permissionManager.getOverlayPermissionIntent(),
                                permissionManager.getAppSettingsIntent()
                            )
                        }
                    }
                )
                STEP_USAGE -> PermissionStepContent(
                    title = "Allow BePresent to monitor screen time",
                    subtitle = "Your data stays 100% on your phone.",
                    step = "2 of 3",
                    buttonTitle = "Allow Usage Access",
                    onContinue = {
                        if (usageGranted) {
                            currentStep = nextStep(overlayGranted, usageGranted, accessibilityGranted)
                        } else {
                            openSettings(
                                context,
                                permissionManager.getUsageAccessIntent(),
                                permissionManager.getAppSettingsIntent()
                            )
                        }
                    }
                )
                STEP_ACCESSIBILITY -> PermissionStepContent(
                    title = "Enable Accessibility to detect active apps",
                    subtitle = "This lets BePresent detect which app is open so it can block distractions. We never read your screen content.",
                    step = "3 of 3",
                    buttonTitle = "Enable Accessibility",
                    onContinue = {
                        if (accessibilityGranted) {
                            currentStep = nextStep(overlayGranted, usageGranted, accessibilityGranted)
                        } else {
                            showAccessibilityDialog = true
                        }
                    }
                )
            }
        }
    }

    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text("Why Accessibility is needed") },
            text = {
                Text("BePresent uses accessibility to detect which app you're using so it can block distractions during focus sessions. We never read or store your screen content.")
            },
            confirmButton = {
                Button(onClick = {
                    showAccessibilityDialog = false
                    openSettings(
                        context,
                        permissionManager.getAccessibilitySettingsIntent(),
                        permissionManager.getAppSettingsIntent()
                    )
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PermissionStepContent(
    title: String,
    subtitle: String,
    step: String,
    buttonTitle: String,
    onContinue: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = step,
            style = OnboardingTypography.subLabel,
            color = OnboardingTokens.Neutral800
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = OnboardingTypography.h2,
            color = OnboardingTokens.NeutralBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subtitle,
            style = OnboardingTypography.p3,
            color = OnboardingTokens.Neutral800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OnboardingContinueButton(
            title = buttonTitle,
            appearance = OnboardingButtonAppearance.Primary,
            onClick = onContinue
        )
    }
}

private fun nextStep(overlay: Boolean, usage: Boolean, accessibility: Boolean): Int = when {
    !overlay -> STEP_OVERLAY
    !usage -> STEP_USAGE
    !accessibility -> STEP_ACCESSIBILITY
    else -> STEP_DONE
}

private fun openSettings(context: Context, intent: Intent, fallback: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(fallback)
    }
}
