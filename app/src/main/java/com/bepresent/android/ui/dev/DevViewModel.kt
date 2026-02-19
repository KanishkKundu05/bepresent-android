package com.bepresent.android.ui.dev

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.db.PresentSessionDao
import com.bepresent.android.data.usage.UsageStatsRepository
import com.bepresent.android.debug.RuntimeLog
import com.bepresent.android.features.blocking.BlockedAppActivity
import com.bepresent.android.features.intentions.IntentionManager
import com.bepresent.android.features.sessions.SessionManager
import com.bepresent.android.permissions.PermissionManager
import com.bepresent.android.service.MonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevUiState(
    val intentions: List<AppIntention> = emptyList(),
    val activeSession: PresentSession? = null,
    val foregroundApp: String? = null,
    val blockedPackages: Set<String> = emptySet(),
    val permissions: PermissionManager.PermissionStatus = PermissionManager.PermissionStatus(
        usageStats = false, notifications = false, batteryOptimization = false, overlay = false
    ),
    val totalXp: Int = 0,
    val totalCoins: Int = 0,
    val streakFreezeAvailable: Boolean = false,
    val activeSessionId: String? = null,
    val runtimeLogs: List<String> = emptyList()
)

@HiltViewModel
class DevViewModel @Inject constructor(
    application: Application,
    private val intentionManager: IntentionManager,
    private val sessionManager: SessionManager,
    private val usageStatsRepository: UsageStatsRepository,
    private val permissionManager: PermissionManager,
    private val preferencesManager: PreferencesManager,
    private val intentionDao: AppIntentionDao,
    private val sessionDao: PresentSessionDao
) : AndroidViewModel(application) {

    private val _foregroundApp = MutableStateFlow<String?>(null)
    private val _blockedPackages = MutableStateFlow<Set<String>>(emptySet())
    private val _permissions = MutableStateFlow(permissionManager.checkAll())

    private val baseUiState: Flow<DevUiState> = combine(
        intentionManager.observeAll(),
        sessionManager.observeActiveSession(),
        _foregroundApp,
        _blockedPackages,
        combine(
            preferencesManager.totalXp,
            preferencesManager.totalCoins,
            preferencesManager.streakFreezeAvailable,
            preferencesManager.activeSessionId,
            _permissions
        ) { xp, coins, freeze, sessionId, perms ->
            DataGroup(xp, coins, freeze, sessionId, perms)
        }
    ) { intentions, session, fg, blocked, data ->
        DevUiState(
            intentions = intentions,
            activeSession = session,
            foregroundApp = fg,
            blockedPackages = blocked,
            permissions = data.permissions,
            totalXp = data.xp,
            totalCoins = data.coins,
            streakFreezeAvailable = data.freeze,
            activeSessionId = data.sessionId
        )
    }

    val uiState: StateFlow<DevUiState> = combine(baseUiState, RuntimeLog.entries) { state, logs ->
        state.copy(runtimeLogs = logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DevUiState())

    private data class DataGroup(
        val xp: Int,
        val coins: Int,
        val freeze: Boolean,
        val sessionId: String?,
        val permissions: PermissionManager.PermissionStatus
    )

    init {
        RuntimeLog.i(TAG, "DevViewModel initialized")
        // Poll foreground app and blocked packages every 2s
        viewModelScope.launch {
            while (isActive) {
                try {
                    _foregroundApp.value = usageStatsRepository.detectForegroundApp()
                    _blockedPackages.value = intentionManager.getBlockedPackages()
                    _permissions.value = permissionManager.checkAll()
                } catch (_: Exception) {}
                delay(2000)
            }
        }
    }

    fun createIntention(packageName: String, appName: String) {
        viewModelScope.launch {
            intentionManager.create(
                packageName = packageName,
                appName = appName,
                allowedOpensPerDay = 3,
                timePerOpenMinutes = 5
            )
        }
    }

    fun openApp(intentionId: String) {
        viewModelScope.launch {
            intentionManager.openApp(intentionId)
        }
    }

    fun reblockApp(intentionId: String) {
        viewModelScope.launch {
            intentionManager.reblockApp(intentionId)
        }
    }

    fun deleteIntention(intention: AppIntention) {
        viewModelScope.launch {
            intentionManager.delete(intention)
        }
    }

    fun resetDaily(intentionId: String) {
        viewModelScope.launch {
            val intention = intentionDao.getById(intentionId) ?: return@launch
            intentionDao.upsert(intention.copy(totalOpensToday = 0, currentlyOpen = false, openedAt = null))
        }
    }

    fun startMonitoring() {
        RuntimeLog.i(TAG, "startMonitoring from Dev tools")
        MonitoringService.start(getApplication())
    }

    fun stopMonitoring() {
        RuntimeLog.i(TAG, "stopMonitoring from Dev tools")
        MonitoringService.stop(getApplication())
    }

    fun launchTestShield(shieldType: String) {
        val packageName = _foregroundApp.value
            ?: _blockedPackages.value.firstOrNull()
            ?: "com.instagram.android"
        RuntimeLog.i(TAG, "launchTestShield: type=$shieldType package=$packageName")
        val intent = Intent(getApplication(), BlockedAppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(BlockedAppActivity.EXTRA_SHIELD_TYPE, shieldType)
        }
        getApplication<Application>().startActivity(intent)
    }

    fun clearRuntimeLogs() {
        RuntimeLog.clear()
    }

    companion object {
        private const val TAG = "BP_Dev"
    }
}
