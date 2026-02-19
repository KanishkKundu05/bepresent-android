package com.bepresent.android.features.sessions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bepresent.android.debug.RuntimeLog
import com.bepresent.android.service.SessionAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleGoalAlarm(sessionId: String, triggerTime: Long) {
        RuntimeLog.i(TAG, "scheduleGoalAlarm: sessionId=$sessionId triggerAt=$triggerTime")
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
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, null), pending)
    }

    fun cancelGoalAlarm(sessionId: String) {
        RuntimeLog.i(TAG, "cancelGoalAlarm: sessionId=$sessionId")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SessionAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    companion object {
        private const val TAG = "BP_SessionAlarmSched"
    }
}
