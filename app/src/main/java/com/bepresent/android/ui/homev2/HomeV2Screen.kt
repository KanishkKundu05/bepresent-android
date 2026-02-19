package com.bepresent.android.ui.homev2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.ui.homev2.components.ActiveSessionCard
import com.bepresent.android.ui.homev2.components.BlockedTimeCard
import com.bepresent.android.ui.homev2.components.DailyQuestCard
import com.bepresent.android.ui.homev2.components.HomeDateCarousel
import com.bepresent.android.ui.homev2.components.HomeHeaderRow
import com.bepresent.android.ui.homev2.components.IntentionsCard
import com.bepresent.android.ui.homev2.components.SessionCountdownCard
import com.bepresent.android.ui.homev2.components.SessionGoalSheet
import com.bepresent.android.ui.homev2.components.SessionModeSheet
import com.bepresent.android.ui.intention.IntentionConfigSheet
import com.bepresent.android.ui.picker.AppPickerSheet
import com.bepresent.android.ui.picker.InstalledApp

@Composable
fun HomeV2Screen(
    viewModel: HomeV2ViewModel,
    onProfileClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onDevClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sheet states
    var showModeSheet by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showIntentionConfig by remember { mutableStateOf(false) }
    var selectedAppForIntention by remember { mutableStateOf<InstalledApp?>(null) }
    var editingIntention by remember { mutableStateOf<AppIntention?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        BackgroundV2()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header (not scrollable)
            HomeHeaderRow(
                streak = uiState.streak,
                isStreakFrozen = uiState.isStreakFrozen,
                weeklyXp = uiState.weeklyXp,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(top = 48.dp, bottom = 10.dp)
            )

            // Scrollable body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Date carousel (only in idle state)
                if (uiState.screenState == HomeScreenState.Idle) {
                    HomeDateCarousel(
                        days = uiState.days,
                        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Main card — state-switched
                CardV2(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (uiState.screenState) {
                        HomeScreenState.Idle -> {
                            BlockedTimeCard(
                                state = uiState.blockedTimeState,
                                onSessionModeClick = { showModeSheet = true },
                                onSessionGoalClick = { showGoalSheet = true },
                                onBlockNowClick = { viewModel.startCountdown() }
                            )
                        }
                        HomeScreenState.Countdown -> {
                            SessionCountdownCard(
                                count = uiState.countdownValue,
                                onCancel = { viewModel.cancelCountdown() }
                            )
                        }
                        HomeScreenState.ActiveSession -> {
                            ActiveSessionCard(
                                state = uiState.activeSessionState,
                                onTakeBreak = { /* TODO: break flow */ },
                                onEndBreak = { /* TODO: end break */ },
                                onGiveUp = { viewModel.giveUpSession() },
                                onBeastModeInfo = { /* TODO: beast mode info */ }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Intentions card
                CardV2(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IntentionsCard(
                        intentions = uiState.intentions,
                        onReload = { /* TODO: reload intentions */ },
                        onAdd = {
                            showAppPicker = true
                        },
                        onIntentionClick = { intention ->
                            editingIntention = intention
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Daily Quest card
                CardV2(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DailyQuestCard(state = uiState.dailyQuestState)
                }

                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for tab bar
            }
        }
    }

    // --- Bottom Sheets ---

    if (showModeSheet) {
        SessionModeSheet(
            currentModeIndex = uiState.sessionModeIndex,
            onDismiss = { showModeSheet = false },
            onSetMode = { index ->
                viewModel.setSessionMode(index)
                showModeSheet = false
            }
        )
    }

    if (showGoalSheet) {
        SessionGoalSheet(
            currentDurationMinutes = uiState.sessionDurationMinutes,
            currentBeastMode = uiState.sessionBeastMode,
            onDismiss = { showGoalSheet = false },
            onSetGoal = { duration, beast ->
                viewModel.setSessionGoal(duration, beast)
                showGoalSheet = false
            }
        )
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
}
