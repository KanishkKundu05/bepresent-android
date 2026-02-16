package com.bepresent.android.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bepresent.android.data.convex.ConvexManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val totalXp: Int,
    val maxStreak: Int,
    val totalFocusMinutes: Int,
    val isMe: Boolean = false
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val convexManager: ConvexManager
) : ViewModel() {

    private val _globalLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = _globalLeaderboard

    private val _friendsLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val friendsLeaderboard: StateFlow<List<LeaderboardEntry>> = _friendsLeaderboard

    val isAuthenticated: Boolean
        get() = convexManager.isAuthenticated

    init {
        subscribeToGlobal()
        subscribeToFriends()
    }

    private fun subscribeToGlobal() {
        viewModelScope.launch {
            if (!convexManager.isAuthenticated) return@launch
            try {
                convexManager.client.subscribe<List<Map<String, Any?>>>("leaderboard:getGlobal")
                    .collect { result ->
                        result.onSuccess { list ->
                            _globalLeaderboard.value = list.mapNotNull { it.toLeaderboardEntry() }
                        }
                    }
            } catch (_: Exception) {}
        }
    }

    private fun subscribeToFriends() {
        viewModelScope.launch {
            if (!convexManager.isAuthenticated) return@launch
            try {
                convexManager.client.subscribe<List<Map<String, Any?>>>("leaderboard:getFriends")
                    .collect { result ->
                        result.onSuccess { list ->
                            _friendsLeaderboard.value = list.mapNotNull { it.toLeaderboardEntry() }
                        }
                    }
            } catch (_: Exception) {}
        }
    }

    private fun Map<String, Any?>.toLeaderboardEntry(): LeaderboardEntry? {
        return try {
            LeaderboardEntry(
                rank = (this["rank"] as? Number)?.toInt() ?: 0,
                userId = this["userId"] as? String ?: "",
                displayName = this["displayName"] as? String ?: "Unknown",
                totalXp = (this["totalXp"] as? Number)?.toInt() ?: 0,
                maxStreak = (this["maxStreak"] as? Number)?.toInt() ?: 0,
                totalFocusMinutes = (this["totalFocusMinutes"] as? Number)?.toInt() ?: 0,
                isMe = this["isMe"] as? Boolean ?: false
            )
        } catch (_: Exception) {
            null
        }
    }
}
