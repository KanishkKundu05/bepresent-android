package com.bepresent.android.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.permissions.OemBatteryGuide
import com.bepresent.android.permissions.PermissionManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

private const val STEP_WELCOME = 0
private const val STEP_USAGE_ACCESS = 1
private const val STEP_NOTIFICATIONS = 2
private const val STEP_BATTERY = 3
private const val STEP_OVERLAY = 4
private const val STEP_DONE = 5

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            OnboardingEntryPoint::class.java
        )
    }
    val permissionManager = remember { entryPoint.permissionManager() }
    val preferencesManager = remember { entryPoint.preferencesManager() }

    var currentStep by remember { mutableIntStateOf(STEP_WELCOME) }
    var usageGranted by remember { mutableStateOf(permissionManager.hasUsageStatsPermission()) }
    var notificationsGranted by remember { mutableStateOf(permissionManager.hasNotificationPermission()) }
    var batteryGranted by remember { mutableStateOf(permissionManager.isBatteryOptimizationDisabled()) }
    var overlayGranted by remember { mutableStateOf(permissionManager.hasOverlayPermission()) }

    // Re-check permissions when resuming (user comes back from Settings)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            usageGranted = permissionManager.hasUsageStatsPermission()
            notificationsGranted = permissionManager.hasNotificationPermission()
            batteryGranted = permissionManager.isBatteryOptimizationDisabled()
            overlayGranted = permissionManager.hasOverlayPermission()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
        currentStep = STEP_BATTERY
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = currentStep, label = "onboarding") { step ->
                when (step) {
                    STEP_WELCOME -> OnboardingPage(
                        emoji = "\uD83E\uDDD8",
                        title = "Be Present",
                        subtitle = "Be intentional with your phone",
                        buttonText = "Get Started",
                        onAction = { currentStep = STEP_USAGE_ACCESS }
                    )
                    STEP_USAGE_ACCESS -> OnboardingPage(
                        emoji = "\uD83D\uDCCA",
                        title = "Usage Access",
                        subtitle = "BePresent needs to see which apps you use to help you set limits",
                        buttonText = if (usageGranted) "Continue" else "Grant Access",
                        onAction = {
                            if (usageGranted) {
                                currentStep = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    STEP_NOTIFICATIONS
                                } else {
                                    STEP_BATTERY
                                }
                            } else {
                                context.startActivity(permissionManager.getUsageAccessIntent())
                            }
                        },
                        showWarning = !usageGranted && currentStep == STEP_USAGE_ACCESS
                    )
                    STEP_NOTIFICATIONS -> OnboardingPage(
                        emoji = "\uD83D\uDD14",
                        title = "Notifications",
                        subtitle = "Get notified when your app time is up and sessions complete",
                        buttonText = if (notificationsGranted) "Continue" else "Enable Notifications",
                        onAction = {
                            if (notificationsGranted) {
                                currentStep = STEP_BATTERY
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        secondaryText = "Skip",
                        onSecondary = { currentStep = STEP_BATTERY }
                    )
                    STEP_BATTERY -> OnboardingPage(
                        emoji = "\uD83D\uDD0B",
                        title = "Battery Optimization",
                        subtitle = "Keep BePresent running reliably in the background",
                        buttonText = if (batteryGranted) "Continue" else "Disable Battery Optimization",
                        onAction = {
                            if (batteryGranted) {
                                currentStep = STEP_OVERLAY
                            } else {
                                context.startActivity(permissionManager.getBatteryOptimizationIntent())
                            }
                        },
                        secondaryText = "Skip",
                        onSecondary = { currentStep = STEP_OVERLAY },
                        extraContent = {
                            OemBatteryGuide.getInstructions()?.let { guide ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${guide.manufacturer} Users",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = guide.steps,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    )
                    STEP_OVERLAY -> OnboardingPage(
                        emoji = "\uD83D\uDDD4",
                        title = "Display Over Apps",
                        subtitle = "BePresent needs to display over other apps to show the shield when you open a blocked app",
                        buttonText = if (overlayGranted) "Continue" else "Grant Permission",
                        onAction = {
                            if (overlayGranted) {
                                currentStep = STEP_DONE
                            } else {
                                context.startActivity(permissionManager.getOverlayPermissionIntent())
                            }
                        },
                        secondaryText = "Skip",
                        onSecondary = { currentStep = STEP_DONE }
                    )
                    STEP_DONE -> OnboardingPage(
                        emoji = "\u2705",
                        title = "You're Ready!",
                        subtitle = "Time to be present",
                        buttonText = "Open Dashboard",
                        onAction = {
                            scope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                                onComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    emoji: String,
    title: String,
    subtitle: String,
    buttonText: String,
    onAction: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    showWarning: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emoji,
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showWarning) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This permission is required for BePresent to work",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        extraContent?.invoke()
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = buttonText, style = MaterialTheme.typography.titleMedium)
        }
        if (secondaryText != null && onSecondary != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSecondary) {
                Text(text = secondaryText)
            }
        }
    }
}
