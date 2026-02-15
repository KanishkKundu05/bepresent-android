package com.bepresent.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.usage.AppUsageInfo
import com.bepresent.android.data.usage.UsageStatsRepository
import com.bepresent.android.features.intentions.IntentionManager
import com.bepresent.android.features.sessions.SessionManager
import com.bepresent.android.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalScreenTimeMs: Long = 0L,
    val perAppUsage: List<AppUsageInfo> = emptyList(),
    val intentions: List<AppIntention> = emptyList(),
    val activeSession: PresentSession? = null,
    val totalXp: Int = 0,
    val totalCoins: Int = 0,
    val maxStreak: Int = 0,
    val permissionsOk: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val intentionManager: IntentionManager,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _screenTime = MutableStateFlow(0L)
    private val _perAppUsage = MutableStateFlow<List<AppUsageInfo>>(emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        _screenTime,
        _perAppUsage,
        intentionManager.observeAll(),
        sessionManager.observeActiveSession(),
        preferencesManager.totalXp,
        preferencesManager.totalCoins
    ) { values ->
        val screenTime = values[0] as Long
        @Suppress("UNCHECKED_CAST")
        val perApp = values[1] as List<AppUsageInfo>
        @Suppress("UNCHECKED_CAST")
        val intentions = values[2] as List<AppIntention>
        val session = values[3] as PresentSession?
        val xp = values[4] as Int
        val coins = values[5] as Int

        DashboardUiState(
            totalScreenTimeMs = screenTime,
            perAppUsage = perApp,
            intentions = intentions,
            activeSession = session,
            totalXp = xp,
            totalCoins = coins,
            maxStreak = intentions.maxOfOrNull { it.streak } ?: 0,
            permissionsOk = permissionManager.checkAll().criticalGranted
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        // Poll screen time every 30 seconds
        viewModelScope.launch {
            while (isActive) {
                refreshScreenTime()
                delay(30_000)
            }
        }
    }

    fun refreshScreenTime() {
        viewModelScope.launch {
            try {
                _screenTime.value = usageStatsRepository.getTotalScreenTimeToday()
                _perAppUsage.value = usageStatsRepository.getPerAppScreenTime()
            } catch (_: Exception) {
                // Permission might not be granted yet
            }
        }
    }

    fun createIntention(
        packageName: String,
        appName: String,
        allowedOpensPerDay: Int,
        timePerOpenMinutes: Int
    ) {
        viewModelScope.launch {
            intentionManager.create(packageName, appName, allowedOpensPerDay, timePerOpenMinutes)
        }
    }

    fun updateIntention(intention: AppIntention) {
        viewModelScope.launch {
            intentionManager.update(intention)
        }
    }

    fun deleteIntention(intention: AppIntention) {
        viewModelScope.launch {
            intentionManager.delete(intention)
        }
    }

    fun startSession(
        name: String,
        durationMinutes: Int,
        blockedPackages: List<String>,
        beastMode: Boolean
    ) {
        viewModelScope.launch {
            sessionManager.createAndStart(name, durationMinutes, blockedPackages, beastMode)
        }
    }

    fun giveUpSession() {
        viewModelScope.launch {
            val session = sessionManager.getActiveSession() ?: return@launch
            sessionManager.giveUp(session.id)
        }
    }

    fun completeSession() {
        viewModelScope.launch {
            val session = sessionManager.getActiveSession() ?: return@launch
            sessionManager.complete(session.id)
        }
    }

    fun cancelSession() {
        viewModelScope.launch {
            val session = sessionManager.getActiveSession() ?: return@launch
            sessionManager.cancel(session.id)
        }
    }
}
