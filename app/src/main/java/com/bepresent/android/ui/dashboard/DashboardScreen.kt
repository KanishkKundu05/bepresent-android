package com.bepresent.android.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.ui.components.IntentionRow
import com.bepresent.android.ui.components.ScreenTimeCard
import com.bepresent.android.ui.components.SessionCta
import com.bepresent.android.ui.components.formatDuration
import com.bepresent.android.ui.intention.IntentionConfigSheet
import com.bepresent.android.ui.picker.AppPickerSheet
import com.bepresent.android.ui.picker.InstalledApp
import com.bepresent.android.ui.session.SessionConfigSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Sheet states
    var showAppPicker by remember { mutableStateOf(false) }
    var appPickerMode by remember { mutableStateOf("intention") } // "intention" or "session"
    var showIntentionConfig by remember { mutableStateOf(false) }
    var selectedAppForIntention by remember { mutableStateOf<InstalledApp?>(null) }
    var editingIntention by remember { mutableStateOf<AppIntention?>(null) }
    var showSessionConfig by remember { mutableStateOf(false) }
    val sessionSelectedApps = remember { mutableStateListOf<InstalledApp>() }
    var showSessionAppPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BePresent",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (uiState.maxStreak > 0) {
                            Text(
                                text = "\uD83D\uDD25 ${uiState.maxStreak}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "\u2B50 ${uiState.totalXp}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Permission banner
            if (!uiState.permissionsOk) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "BePresent can't monitor apps \u2014 tap to fix",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Screen Time Card
            ScreenTimeCard(
                totalScreenTimeMs = uiState.totalScreenTimeMs,
                perAppUsage = uiState.perAppUsage,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Active Session Card
            uiState.activeSession?.let { session ->
                ActiveSessionCard(
                    session = session,
                    onGiveUp = { viewModel.giveUpSession() },
                    onComplete = { viewModel.completeSession() },
                    onCancel = { viewModel.cancelSession() }
                )
            }

            // Intentions Row
            IntentionRow(
                intentions = uiState.intentions,
                onAddClick = {
                    appPickerMode = "intention"
                    showAppPicker = true
                },
                onIntentionClick = { intention ->
                    editingIntention = intention
                }
            )

            // Start Session CTA (only if no active session)
            if (uiState.activeSession == null) {
                SessionCta(onClick = { showSessionConfig = true })
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // App Picker for Intentions
    if (showAppPicker) {
        val existingPackages = uiState.intentions.map { it.packageName }.toSet()
        AppPickerSheet(
            multiSelect = false,
            excludePackages = existingPackages,
            onDismiss = { showAppPicker = false },
            onAppsSelected = { apps ->
                showAppPicker = false
                if (apps.isNotEmpty()) {
                    selectedAppForIntention = apps.first()
                    showIntentionConfig = true
                }
            }
        )
    }

    // Intention Config (new)
    if (showIntentionConfig && selectedAppForIntention != null) {
        IntentionConfigSheet(
            appName = selectedAppForIntention!!.label,
            onDismiss = {
                showIntentionConfig = false
                selectedAppForIntention = null
            },
            onSave = { opens, time ->
                viewModel.createIntention(
                    packageName = selectedAppForIntention!!.packageName,
                    appName = selectedAppForIntention!!.label,
                    allowedOpensPerDay = opens,
                    timePerOpenMinutes = time
                )
                showIntentionConfig = false
                selectedAppForIntention = null
            }
        )
    }

    // Intention Config (edit)
    if (editingIntention != null) {
        IntentionConfigSheet(
            appName = editingIntention!!.appName,
            existingIntention = editingIntention,
            onDismiss = { editingIntention = null },
            onSave = { opens, time ->
                viewModel.updateIntention(
                    editingIntention!!.copy(
                        allowedOpensPerDay = opens,
                        timePerOpenMinutes = time
                    )
                )
                editingIntention = null
            },
            onDelete = {
                viewModel.deleteIntention(editingIntention!!)
                editingIntention = null
            }
        )
    }

    // Session Config
    if (showSessionConfig) {
        SessionConfigSheet(
            onDismiss = {
                showSessionConfig = false
                sessionSelectedApps.clear()
            },
            onOpenAppPicker = { showSessionAppPicker = true },
            selectedApps = sessionSelectedApps,
            onStart = { name, duration, beastMode ->
                viewModel.startSession(
                    name = name,
                    durationMinutes = duration,
                    blockedPackages = sessionSelectedApps.map { it.packageName },
                    beastMode = beastMode
                )
                showSessionConfig = false
                sessionSelectedApps.clear()
            }
        )
    }

    // App Picker for Sessions
    if (showSessionAppPicker) {
        AppPickerSheet(
            multiSelect = true,
            onDismiss = { showSessionAppPicker = false },
            onAppsSelected = { apps ->
                sessionSelectedApps.clear()
                sessionSelectedApps.addAll(apps)
                showSessionAppPicker = false
            }
        )
    }
}

@Composable
private fun ActiveSessionCard(
    session: PresentSession,
    onGiveUp: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val isGoalReached = session.state == PresentSession.STATE_GOAL_REACHED
    val elapsed = System.currentTimeMillis() - (session.startedAt ?: System.currentTimeMillis())
    val canCancel = elapsed <= 10_000

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalReached) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isGoalReached) "\uD83C\uDF89 Goal Reached!" else "\uD83D\uDEE1\uFE0F ${session.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isGoalReached) {
                    "Tap Complete to earn your XP, or keep going!"
                } else {
                    "Focus session in progress \u2014 ${formatDuration(elapsed)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGoalReached) {
                    TextButton(onClick = onComplete) {
                        Text("Complete")
                    }
                } else {
                    if (canCancel) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                    if (!session.beastMode) {
                        TextButton(onClick = onGiveUp) {
                            Text("Give Up", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
