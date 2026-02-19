package com.bepresent.android.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bepresent.android.BePresentApp
import com.bepresent.android.debug.RuntimeLog
import com.bepresent.android.data.db.BePresentDatabase
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.db.PresentSessionAction
import com.bepresent.android.features.sessions.SessionStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class SessionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_GOAL_REACHED) return
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        RuntimeLog.i(TAG, "onReceive: goal reached alarm sessionId=$sessionId")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    BePresentDatabase::class.java,
                    "bepresent.db"
                ).build()

                val dao = db.presentSessionDao()
                val session = dao.getById(sessionId)

                if (session != null && session.state == PresentSession.STATE_ACTIVE) {
                    RuntimeLog.i(TAG, "goalReached: transitioning session=${session.id}")
                    val updated = session.copy(
                        state = PresentSession.STATE_GOAL_REACHED,
                        goalReachedAt = System.currentTimeMillis()
                    )
                    dao.upsert(updated)
                    dao.insertAction(
                        PresentSessionAction(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            action = PresentSessionAction.ACTION_GOAL_REACHED
                        )
                    )

                    val (xp, _) = SessionStateMachine.calculateRewards(session.goalDurationMinutes)
                    showGoalReachedNotification(context, session.name, xp)
                } else {
                    RuntimeLog.w(TAG, "goalReached: ignored, session missing or not active")
                }

                db.close()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showGoalReachedNotification(context: Context, sessionName: String, xp: Int) {
        val notification = NotificationCompat.Builder(context, BePresentApp.CHANNEL_SESSION)
            .setContentTitle("Goal Reached!")
            .setContentText("$sessionName — +$xp XP")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(MonitoringService.NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "BP_SessionAlarm"
        const val ACTION_GOAL_REACHED = "com.bepresent.android.SESSION_GOAL_REACHED"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
