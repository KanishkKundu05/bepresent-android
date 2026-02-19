package com.bepresent.android.features.sessions

import android.content.Context
import com.bepresent.android.data.convex.SyncManager
import com.bepresent.android.data.convex.SyncWorker
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.db.PresentSessionAction
import com.bepresent.android.data.db.PresentSessionDao
import com.bepresent.android.service.MonitoringService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDao: PresentSessionDao,
    private val intentionDao: AppIntentionDao,
    private val preferencesManager: PreferencesManager,
    private val syncManager: SyncManager,
    private val sessionAlarmScheduler: SessionAlarmScheduler
) {
    fun observeActiveSession(): Flow<PresentSession?> = sessionDao.observeActiveSession()
    fun observeAllSessions(): Flow<List<PresentSession>> = sessionDao.getAllSessions()

    suspend fun getActiveSession(): PresentSession? = sessionDao.getActiveSession()

    suspend fun createAndStart(
        name: String,
        goalDurationMinutes: Int,
        blockedPackages: List<String>,
        beastMode: Boolean
    ): PresentSession {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val packagesJson = JSONArray(blockedPackages).toString()

        val session = PresentSession(
            id = id,
            name = name,
            goalDurationMinutes = goalDurationMinutes,
            beastMode = beastMode,
            state = PresentSession.STATE_ACTIVE,
            blockedPackages = packagesJson,
            startedAt = now
        )

        sessionDao.upsert(session)
        sessionDao.insertAction(
            PresentSessionAction(
                id = UUID.randomUUID().toString(),
                sessionId = id,
                action = PresentSessionAction.ACTION_START
            )
        )
        preferencesManager.setActiveSessionId(id)
        sessionAlarmScheduler.scheduleGoalAlarm(id, now + (goalDurationMinutes * 60 * 1000L))
        MonitoringService.start(context)

        return session
    }

    suspend fun cancel(sessionId: String): Boolean =
        applyTransition(sessionId, SessionStateMachine::cancel)

    suspend fun giveUp(sessionId: String): Boolean =
        applyTransition(sessionId, SessionStateMachine::giveUp)

    suspend fun goalReached(sessionId: String): Boolean =
        applyTransition(sessionId, SessionStateMachine::goalReached)

    suspend fun complete(sessionId: String): Boolean =
        applyTransition(sessionId, SessionStateMachine::complete)

    fun getBlockedPackagesFromJson(json: String): Set<String> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private suspend fun applyTransition(
        sessionId: String,
        transitionFn: (PresentSession) -> SessionStateMachine.TransitionResult
    ): Boolean {
        val session = sessionDao.getById(sessionId) ?: return false
        val result = transitionFn(session)
        if (result !is SessionStateMachine.TransitionResult.Success) return false

        val transition = result.transition
        val now = System.currentTimeMillis()
        val rewards = if (transition.rewardsEligible) {
            SessionStateMachine.calculateRewards(session.goalDurationMinutes)
        } else {
            null
        }

        val updated = session.copy(
            state = transition.newState,
            endedAt = if (transition.setEndedAt) now else session.endedAt,
            goalReachedAt = if (transition.setGoalReachedAt) now else session.goalReachedAt,
            earnedXp = rewards?.first ?: session.earnedXp,
            earnedCoins = rewards?.second ?: session.earnedCoins
        )

        sessionDao.upsert(updated)
        sessionDao.insertAction(
            PresentSessionAction(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                action = transition.action
            )
        )

        if (transition.cancelAlarm) {
            sessionAlarmScheduler.cancelGoalAlarm(sessionId)
        }

        if (transition.rewardsEligible && rewards != null) {
            preferencesManager.addXpAndCoins(rewards.first, rewards.second)
        }

        if (transition.clearActiveSession) {
            preferencesManager.setActiveSessionId(null)
            MonitoringService.checkAndStop(context, sessionDao, intentionDao)
        }

        if (transition.syncAfter) {
            syncManager.enqueueSessionSync(updated)
            SyncWorker.triggerImmediateSync(context)
        }

        return true
    }
}
