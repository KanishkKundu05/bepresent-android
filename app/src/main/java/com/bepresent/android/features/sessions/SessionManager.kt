package com.bepresent.android.features.sessions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.db.PresentSessionAction
import com.bepresent.android.data.db.PresentSessionDao
import com.bepresent.android.service.MonitoringService
import com.bepresent.android.service.SessionAlarmReceiver
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
    private val preferencesManager: PreferencesManager
) {
    fun observeActiveSession(): Flow<PresentSession?> = sessionDao.observeActiveSession()

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

        // Schedule goal reached alarm
        scheduleGoalAlarm(id, now + (goalDurationMinutes * 60 * 1000L))

        // Start monitoring service
        MonitoringService.start(context)

        return session
    }

    suspend fun cancel(sessionId: String): Boolean {
        val session = sessionDao.getById(sessionId) ?: return false
        val result = SessionStateMachine.cancel(session)
        if (result is SessionStateMachine.TransitionResult.Success) {
            val updated = session.copy(state = result.newState, endedAt = System.currentTimeMillis())
            sessionDao.upsert(updated)
            sessionDao.insertAction(
                PresentSessionAction(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    action = PresentSessionAction.ACTION_CANCEL
                )
            )
            cancelGoalAlarm(sessionId)
            preferencesManager.setActiveSessionId(null)
            MonitoringService.checkAndStop(context)
            return true
        }
        return false
    }

    suspend fun giveUp(sessionId: String): Boolean {
        val session = sessionDao.getById(sessionId) ?: return false
        val result = SessionStateMachine.giveUp(session)
        if (result is SessionStateMachine.TransitionResult.Success) {
            val updated = session.copy(state = result.newState, endedAt = System.currentTimeMillis())
            sessionDao.upsert(updated)
            sessionDao.insertAction(
                PresentSessionAction(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    action = PresentSessionAction.ACTION_GIVE_UP
                )
            )
            cancelGoalAlarm(sessionId)
            preferencesManager.setActiveSessionId(null)
            MonitoringService.checkAndStop(context)
            return true
        }
        return false
    }

    suspend fun goalReached(sessionId: String): Boolean {
        val session = sessionDao.getById(sessionId) ?: return false
        val result = SessionStateMachine.goalReached(session)
        if (result is SessionStateMachine.TransitionResult.Success) {
            val updated = session.copy(
                state = result.newState,
                goalReachedAt = System.currentTimeMillis()
            )
            sessionDao.upsert(updated)
            sessionDao.insertAction(
                PresentSessionAction(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    action = PresentSessionAction.ACTION_GOAL_REACHED
                )
            )
            return true
        }
        return false
    }

    suspend fun complete(sessionId: String): Boolean {
        val session = sessionDao.getById(sessionId) ?: return false
        val result = SessionStateMachine.complete(session)
        if (result is SessionStateMachine.TransitionResult.Success) {
            val (xp, coins) = SessionStateMachine.calculateRewards(session.goalDurationMinutes)
            val updated = session.copy(
                state = result.newState,
                endedAt = System.currentTimeMillis(),
                earnedXp = xp,
                earnedCoins = coins
            )
            sessionDao.upsert(updated)
            sessionDao.insertAction(
                PresentSessionAction(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    action = PresentSessionAction.ACTION_COMPLETE
                )
            )
            preferencesManager.addXpAndCoins(xp, coins)
            preferencesManager.setActiveSessionId(null)
            MonitoringService.checkAndStop(context)
            return true
        }
        return false
    }

    fun getBlockedPackagesFromJson(json: String): Set<String> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun scheduleGoalAlarm(sessionId: String, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            action = SessionAlarmReceiver.ACTION_GOAL_REACHED
            putExtra(SessionAlarmReceiver.EXTRA_SESSION_ID, sessionId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, null),
            pending
        )
    }

    private fun cancelGoalAlarm(sessionId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SessionAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, sessionId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }
}
