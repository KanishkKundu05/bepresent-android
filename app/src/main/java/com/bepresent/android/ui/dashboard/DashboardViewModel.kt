package com.bepresent.android.ui.dashboard

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeCalendarDay(
    val weekDay: String,
    val dayNumber: String,
    val isCurrentDay: Boolean,
    val isEnabled: Boolean,
    val isChecked: Boolean
)

data class DashboardUiState(
    val totalScreenTimeMs: Long = 0L,
    val perAppUsage: List<AppUsageInfo> = emptyList(),
    val intentions: List<AppIntention> = emptyList(),
    val activeSession: PresentSession? = null,
    val totalXp: Int = 0,
    val totalCoins: Int = 0,
    val maxStreak: Int = 0,
    val blockedTodaySeconds: Long = 0L,
    val dailyRecordSeconds: Long = 0L,
    val weekCalendar: List<HomeCalendarDay> = emptyList(),
    val permissionsOk: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageStatsRepository: UsageStatsRepository,
    private val intentionManager: IntentionManager,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _screenTime = MutableStateFlow(0L)
    private val _perAppUsage = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    private val zoneId = ZoneId.systemDefault()
    private val installDate: LocalDate = run {
        val firstInstallMs = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrNull() ?: System.currentTimeMillis()
        Instant.ofEpochMilli(firstInstallMs).atZone(zoneId).toLocalDate()
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _screenTime,
        _perAppUsage,
        intentionManager.observeAll(),
        sessionManager.observeActiveSession(),
        sessionManager.observeAllSessions(),
        preferencesManager.totalXp,
        preferencesManager.totalCoins
    ) { values ->
        val screenTime = values[0] as Long
        @Suppress("UNCHECKED_CAST")
        val perApp = values[1] as List<AppUsageInfo>
        @Suppress("UNCHECKED_CAST")
        val intentions = values[2] as List<AppIntention>
        val session = values[3] as PresentSession?
        @Suppress("UNCHECKED_CAST")
        val allSessions = values[4] as List<PresentSession>
        val xp = values[5] as Int
        val coins = values[6] as Int
        val blockedByDaySeconds = calculateBlockedSecondsByDay(allSessions)
        val today = LocalDate.now()

        DashboardUiState(
            totalScreenTimeMs = screenTime,
            perAppUsage = perApp,
            intentions = intentions,
            activeSession = session,
            totalXp = xp,
            totalCoins = coins,
            maxStreak = intentions.maxOfOrNull { it.streak } ?: 0,
            blockedTodaySeconds = blockedByDaySeconds[today] ?: 0L,
            dailyRecordSeconds = blockedByDaySeconds.values.maxOrNull() ?: 0L,
            weekCalendar = buildCalendarDays(blockedByDaySeconds, today),
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

    private fun calculateBlockedSecondsByDay(sessions: List<PresentSession>): Map<LocalDate, Long> {
        val perSession = sessions.asSequence()
            .filter { it.state == PresentSession.STATE_COMPLETED || it.state == PresentSession.STATE_GAVE_UP }
            .mapNotNull { session ->
                val startedAt = session.startedAt ?: return@mapNotNull null
                val endedAt = session.endedAt ?: return@mapNotNull null
                if (endedAt <= startedAt) return@mapNotNull null
                val date = Instant.ofEpochMilli(startedAt).atZone(zoneId).toLocalDate()
                date to ((endedAt - startedAt) / 1000L)
            }
            .toList()

        return perSession
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .mapValues { (_, durations) -> durations.sum() }
    }

    private fun buildCalendarDays(
        blockedByDaySeconds: Map<LocalDate, Long>,
        today: LocalDate
    ): List<HomeCalendarDay> {
        return (-3..3).map { offset ->
            val day = today.plusDays(offset.toLong())
            HomeCalendarDay(
                weekDay = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                dayNumber = day.dayOfMonth.toString(),
                isCurrentDay = offset == 0,
                isEnabled = !day.isBefore(installDate),
                isChecked = (blockedByDaySeconds[day] ?: 0L) > 0L
            )
        }
    }
}
