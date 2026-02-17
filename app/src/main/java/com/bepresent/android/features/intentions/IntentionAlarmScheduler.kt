package com.bepresent.android.features.intentions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bepresent.android.service.IntentionAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentionAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReblock(intentionId: String, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val warningIntent = Intent(context, IntentionAlarmReceiver::class.java).apply {
            action = IntentionAlarmReceiver.ACTION_WARNING
            putExtra(IntentionAlarmReceiver.EXTRA_INTENTION_ID, intentionId)
        }
        val warningPending = PendingIntent.getBroadcast(
            context,
            intentionId.hashCode() + 1,
            warningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime - 30_000, null), warningPending)

        val reblockIntent = Intent(context, IntentionAlarmReceiver::class.java).apply {
            action = IntentionAlarmReceiver.ACTION_REBLOCK
            putExtra(IntentionAlarmReceiver.EXTRA_INTENTION_ID, intentionId)
        }
        val reblockPending = PendingIntent.getBroadcast(
            context,
            intentionId.hashCode(),
            reblockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, null), reblockPending)
    }

    fun cancelReblock(intentionId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val warningIntent = Intent(context, IntentionAlarmReceiver::class.java)
        val warningPending = PendingIntent.getBroadcast(
            context,
            intentionId.hashCode() + 1,
            warningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(warningPending)

        val reblockIntent = Intent(context, IntentionAlarmReceiver::class.java)
        val reblockPending = PendingIntent.getBroadcast(
            context,
            intentionId.hashCode(),
            reblockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reblockPending)
    }
}
