package com.bepresent.android.features.sessions

import com.bepresent.android.data.db.PresentSession

object SessionStateMachine {

    sealed class TransitionResult {
        data class Success(val newState: String) : TransitionResult()
        data class Error(val message: String) : TransitionResult()
    }

    fun start(session: PresentSession): TransitionResult {
        return if (session.state == PresentSession.STATE_IDLE) {
            TransitionResult.Success(PresentSession.STATE_ACTIVE)
        } else {
            TransitionResult.Error("Cannot start session in state: ${session.state}")
        }
    }

    fun cancel(session: PresentSession): TransitionResult {
        if (session.state != PresentSession.STATE_ACTIVE) {
            return TransitionResult.Error("Cannot cancel session in state: ${session.state}")
        }
        val elapsed = System.currentTimeMillis() - (session.startedAt ?: 0)
        return if (elapsed <= 10_000) {
            TransitionResult.Success(PresentSession.STATE_CANCELED)
        } else {
            TransitionResult.Error("Cannot cancel after 10 seconds — use Give Up instead")
        }
    }

    fun giveUp(session: PresentSession): TransitionResult {
        if (session.state != PresentSession.STATE_ACTIVE) {
            return TransitionResult.Error("Cannot give up session in state: ${session.state}")
        }
        return if (!session.beastMode) {
            TransitionResult.Success(PresentSession.STATE_GAVE_UP)
        } else {
            TransitionResult.Error("Beast Mode is enabled — cannot give up")
        }
    }

    fun goalReached(session: PresentSession): TransitionResult {
        return if (session.state == PresentSession.STATE_ACTIVE) {
            TransitionResult.Success(PresentSession.STATE_GOAL_REACHED)
        } else {
            TransitionResult.Error("Cannot reach goal in state: ${session.state}")
        }
    }

    fun complete(session: PresentSession): TransitionResult {
        return if (session.state == PresentSession.STATE_GOAL_REACHED) {
            TransitionResult.Success(PresentSession.STATE_COMPLETED)
        } else {
            TransitionResult.Error("Cannot complete session in state: ${session.state}")
        }
    }

    fun calculateRewards(goalDurationMinutes: Int): Pair<Int, Int> {
        val (xp, coins) = when {
            goalDurationMinutes <= 15 -> 3 to 3
            goalDurationMinutes <= 30 -> 5 to 5
            goalDurationMinutes <= 45 -> 8 to 8
            goalDurationMinutes <= 60 -> 10 to 10
            goalDurationMinutes <= 90 -> 15 to 15
            else -> 25 to 25
        }
        return xp to coins
    }
}
