package com.bepresent.android.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STEP_OVERLAY = 0
private const val STEP_USAGE_ACCESS = 1
private const val STEP_ACCESSIBILITY = 2
private const val STEP_COMPLETE = 3
private const val TOTAL_PERMISSIONS = 3

private enum class LearnMoreTopic {
    OVERLAY,
    USAGE,
    ACCESSIBILITY
}

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

    var overlayGranted by remember { mutableStateOf(permissionManager.hasOverlayPermission()) }
    var usageGranted by remember { mutableStateOf(permissionManager.hasUsageStatsPermission()) }
    var accessibilityGranted by remember { mutableStateOf(permissionManager.hasAccessibilityPermission()) }

    var currentStep by remember {
        mutableIntStateOf(nextMissingStep(overlayGranted, usageGranted, accessibilityGranted))
    }
    var learnMoreTopic by remember { mutableStateOf<LearnMoreTopic?>(null) }
    var showAccessibilityWhyDialog by remember { mutableStateOf(false) }

    var onboardingFinished by remember { mutableStateOf(false) }
    var lastGrantedCount by remember {
        mutableIntStateOf(grantedCount(overlayGranted, usageGranted, accessibilityGranted))
    }
    var progressPopupCount by remember { mutableIntStateOf(0) }

    val completeOnboarding: () -> Unit = {
        if (!onboardingFinished) {
            onboardingFinished = true
            scope.launch {
                preferencesManager.setOnboardingCompleted(true)
                onComplete()
            }
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val newOverlay = permissionManager.hasOverlayPermission()
            val newUsage = permissionManager.hasUsageStatsPermission()
            val newAccessibility = permissionManager.hasAccessibilityPermission()

            overlayGranted = newOverlay
            usageGranted = newUsage
            accessibilityGranted = newAccessibility

            val newGrantedCount = grantedCount(newOverlay, newUsage, newAccessibility)
            if (newGrantedCount > lastGrantedCount) {
                progressPopupCount = newGrantedCount
                lastGrantedCount = newGrantedCount
            }

            currentStep = nextMissingStep(newOverlay, newUsage, newAccessibility)
        }
    }

    LaunchedEffect(progressPopupCount) {
        if (progressPopupCount in 1 until TOTAL_PERMISSIONS) {
            delay(1400)
            progressPopupCount = 0
        }
    }

    LaunchedEffect(currentStep, progressPopupCount, lastGrantedCount) {
        if (currentStep == STEP_COMPLETE && progressPopupCount == 0 && lastGrantedCount == TOTAL_PERMISSIONS) {
            completeOnboarding()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = currentStep.coerceIn(STEP_OVERLAY, STEP_ACCESSIBILITY),
                label = "permission_onboarding"
            ) { step ->
                when (step) {
                    STEP_OVERLAY -> PermissionPage(
                        title = "Enable BePresent to block distracting apps",
                        subtitle = "Don't worry, you can take a break any time.",
                        firstInstruction = "Find BePresent in the list of apps",
                        secondInstruction = "Toggle \"Allow Display over other apps\"",
                        onContinue = {
                            if (overlayGranted) {
                                currentStep = nextMissingStep(overlayGranted, usageGranted, accessibilityGranted)
                            } else {
                                openSettings(
                                    context = context,
                                    permissionIntent = permissionManager.getOverlayPermissionIntent(),
                                    fallbackIntent = permissionManager.getAppSettingsIntent()
                                )
                            }
                        },
                        onLearnMore = { learnMoreTopic = LearnMoreTopic.OVERLAY }
                    )

                    STEP_USAGE_ACCESS -> PermissionPage(
                        title = "Allow BePresent to Monitor Screentime",
                        subtitle = "All your information is secure and will stay 100% on your phone.",
                        firstInstruction = "Find BePresent in the list of apps",
                        secondInstruction = "Toggle \"Permit Usage Access\"",
                        onContinue = {
                            if (usageGranted) {
                                currentStep = nextMissingStep(overlayGranted, usageGranted, accessibilityGranted)
                            } else {
                                openSettings(
                                    context = context,
                                    permissionIntent = permissionManager.getUsageAccessIntent(),
                                    fallbackIntent = permissionManager.getAppSettingsIntent()
                                )
                            }
                        },
                        onLearnMore = { learnMoreTopic = LearnMoreTopic.USAGE }
                    )

                    STEP_ACCESSIBILITY -> PermissionPage(
                        title = "Enable Accessibility permission",
                        subtitle = "This lets BePresent detect the app currently active, so it can block distractions. We never track or read your screen content.",
                        firstInstruction = "Find BePresent in the list of services",
                        secondInstruction = "Toggle BePresent accessibility on",
                        onContinue = {
                            if (accessibilityGranted) {
                                currentStep = nextMissingStep(overlayGranted, usageGranted, accessibilityGranted)
                            } else {
                                showAccessibilityWhyDialog = true
                            }
                        },
                        onLearnMore = { learnMoreTopic = LearnMoreTopic.ACCESSIBILITY }
                    )
                    else -> Unit
                }
            }
        }
    }

    if (showAccessibilityWhyDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityWhyDialog = false },
            title = { Text("Why Accessibility is needed") },
            text = {
                Text(
                    "BePresent uses accessibility permission to detect which app you're currently using so it can block distractions during a focus session. We do not read, record, or collect your screen content."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityWhyDialog = false
                        openSettings(
                            context = context,
                            permissionIntent = permissionManager.getAccessibilitySettingsIntent(),
                            fallbackIntent = permissionManager.getAppSettingsIntent()
                        )
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityWhyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (learnMoreTopic != null) {
        val (title, body) = when (learnMoreTopic) {
            LearnMoreTopic.OVERLAY -> "Why overlay is needed" to
                "Overlay permission lets BePresent show the blocking shield above distracting apps so you can immediately return to your focus session."
            LearnMoreTopic.USAGE -> "Why usage access is needed" to
                "Usage access lets BePresent detect app launches and screen-time usage directly on your phone. Your usage data stays local."
            LearnMoreTopic.ACCESSIBILITY -> "Why accessibility is needed" to
                "Accessibility lets BePresent detect the active app during focus sessions to enforce blocks. Screen contents are never read or stored."
            null -> "" to ""
        }

        AlertDialog(
            onDismissRequest = { learnMoreTopic = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                Button(onClick = { learnMoreTopic = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (progressPopupCount > 0) {
        PermissionProgressPopup(
            grantedCount = progressPopupCount,
            totalCount = TOTAL_PERMISSIONS,
            onDismiss = {
                if (progressPopupCount < TOTAL_PERMISSIONS) {
                    progressPopupCount = 0
                }
            },
            onComplete = {
                progressPopupCount = 0
                completeOnboarding()
            }
        )
    }
}

@Composable
private fun PermissionPage(
    title: String,
    subtitle: String,
    firstInstruction: String,
    secondInstruction: String,
    onContinue: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))
        TwoStepTimeline(
            firstInstruction = firstInstruction,
            secondInstruction = secondInstruction
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onLearnMore) {
            Text("Learn more")
        }
    }
}

@Composable
private fun TwoStepTimeline(
    firstInstruction: String,
    secondInstruction: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            TimelineNumber(1)
            Spacer(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(width = 2.dp, height = 34.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            TimelineNumber(2)
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(26.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = firstInstruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = secondInstruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TimelineNumber(number: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PermissionProgressPopup(
    grantedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var targetProgress by remember(grantedCount) { mutableFloatStateOf(0f) }

    LaunchedEffect(grantedCount) {
        targetProgress = grantedCount.toFloat() / totalCount
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 900),
        label = "permission_progress"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permissions progress",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Card(
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier.size(170.dp)
                        ) {
                            val strokeWidth = 12.dp.toPx()
                            val arcDiameter = size.minDimension - strokeWidth
                            val topLeft = Offset(
                                (size.width - arcDiameter) / 2f,
                                (size.height - arcDiameter) / 2f
                            )

                            drawArc(
                                color = colorScheme.surfaceVariant,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = colorScheme.primary,
                                startAngle = 180f,
                                sweepAngle = 180f * animatedProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Text(
                            text = "$grantedCount/$totalCount",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(colorScheme.primary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (grantedCount == totalCount) {
                Button(onClick = onComplete) {
                    Text("Continue")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        }
    )
}

private fun nextMissingStep(
    overlayGranted: Boolean,
    usageGranted: Boolean,
    accessibilityGranted: Boolean
): Int {
    return when {
        !overlayGranted -> STEP_OVERLAY
        !usageGranted -> STEP_USAGE_ACCESS
        !accessibilityGranted -> STEP_ACCESSIBILITY
        else -> STEP_COMPLETE
    }
}

private fun grantedCount(
    overlayGranted: Boolean,
    usageGranted: Boolean,
    accessibilityGranted: Boolean
): Int {
    var count = 0
    if (overlayGranted) count++
    if (usageGranted) count++
    if (accessibilityGranted) count++
    return count
}

private fun openSettings(
    context: Context,
    permissionIntent: Intent,
    fallbackIntent: Intent
) {
    try {
        context.startActivity(permissionIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(fallbackIntent)
    }
}
