package com.bepresent.android.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "present_sessions")
data class PresentSession(
    @PrimaryKey
    val id: String,
    val name: String,
    val goalDurationMinutes: Int,
    val beastMode: Boolean = false,
    val state: String = "idle",
    val blockedPackages: String, // JSON array of package names
    val startedAt: Long? = null,
    val goalReachedAt: Long? = null,
    val endedAt: Long? = null,
    val earnedXp: Int = 0,
    val earnedCoins: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATE_IDLE = "idle"
        const val STATE_ACTIVE = "active"
        const val STATE_GOAL_REACHED = "goalReached"
        const val STATE_COMPLETED = "completed"
        const val STATE_GAVE_UP = "gaveUp"
        const val STATE_CANCELED = "canceled"
    }
}

@Entity(tableName = "present_session_actions")
data class PresentSessionAction(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val ACTION_START = "start"
        const val ACTION_GIVE_UP = "giveUp"
        const val ACTION_CANCEL = "cancel"
        const val ACTION_GOAL_REACHED = "goalReached"
        const val ACTION_COMPLETE = "complete"
        const val ACTION_EXTEND = "extend"
    }
}
