package com.bepresent.android.ui.partner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bepresent.android.data.convex.ConvexManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartnerStats(
    val displayName: String = "",
    val intentions: List<IntentionSnapshot> = emptyList(),
    val recentSessions: List<SessionSummary> = emptyList(),
    val todayXp: Int = 0,
    val todayFocusMinutes: Int = 0
)

data class IntentionSnapshot(
    val appName: String,
    val streak: Int,
    val allowedOpensPerDay: Int,
    val totalOpensToday: Int
)

data class SessionSummary(
    val name: String,
    val goalDurationMinutes: Int,
    val state: String,
    val earnedXp: Int
)

@HiltViewModel
class PartnerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val convexManager: ConvexManager
) : ViewModel() {

    private val partnerId: String = checkNotNull(savedStateHandle["partnerId"])

    private val _partnerStats = MutableStateFlow(PartnerStats())
    val partnerStats: StateFlow<PartnerStats> = _partnerStats

    init {
        subscribeToPartnerStats()
    }

    @Suppress("UNCHECKED_CAST")
    private fun subscribeToPartnerStats() {
        viewModelScope.launch {
            if (!convexManager.isAuthenticated) return@launch
            val client = convexManager.client ?: return@launch
            try {
                client.subscribe<Map<String, Any?>>(
                    "partners:getPartnerStats",
                    args = mapOf("partnerId" to partnerId)
                ).collect { result ->
                    result.onSuccess { data ->
                        if (data == null) return@onSuccess
                        val intentions = (data["intentions"] as? List<Map<String, Any?>>)?.map { m ->
                            IntentionSnapshot(
                                appName = m["appName"] as? String ?: "",
                                streak = (m["streak"] as? Number)?.toInt() ?: 0,
                                allowedOpensPerDay = (m["allowedOpensPerDay"] as? Number)?.toInt() ?: 0,
                                totalOpensToday = (m["totalOpensToday"] as? Number)?.toInt() ?: 0
                            )
                        } ?: emptyList()

                        val sessions = (data["recentSessions"] as? List<Map<String, Any?>>)?.map { m ->
                            SessionSummary(
                                name = m["name"] as? String ?: "",
                                goalDurationMinutes = (m["goalDurationMinutes"] as? Number)?.toInt() ?: 0,
                                state = m["state"] as? String ?: "",
                                earnedXp = (m["earnedXp"] as? Number)?.toInt() ?: 0
                            )
                        } ?: emptyList()

                        val todayStats = data["todayStats"] as? Map<String, Any?>
                        _partnerStats.value = PartnerStats(
                            displayName = data["displayName"] as? String ?: "",
                            intentions = intentions,
                            recentSessions = sessions,
                            todayXp = (todayStats?.get("totalXp") as? Number)?.toInt() ?: 0,
                            todayFocusMinutes = (todayStats?.get("totalFocusMinutes") as? Number)?.toInt() ?: 0
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
