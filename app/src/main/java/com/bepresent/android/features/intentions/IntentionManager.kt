package com.bepresent.android.features.intentions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bepresent.android.data.db.AppIntention
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.service.IntentionAlarmReceiver
import com.bepresent.android.service.MonitoringService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentionDao: AppIntentionDao
) {
    fun observeAll(): Flow<List<AppIntention>> = intentionDao.getAll()

    suspend fun getAll(): List<AppIntention> = intentionDao.getAllOnce()

    suspend fun getByPackage(packageName: String): AppIntention? =
        intentionDao.getByPackage(packageName)

    suspend fun getById(id: String): AppIntention? = intentionDao.getById(id)

    suspend fun create(
        packageName: String,
        appName: String,
        allowedOpensPerDay: Int,
        timePerOpenMinutes: Int
    ): AppIntention {
        val intention = AppIntention(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = appName,
            allowedOpensPerDay = allowedOpensPerDay,
            timePerOpenMinutes = timePerOpenMinutes
        )
        intentionDao.upsert(intention)
        ensureMonitoringServiceRunning()
        return intention
    }

    suspend fun update(intention: AppIntention) {
        intentionDao.upsert(intention)
    }

    suspend fun delete(intention: AppIntention) {
        // Cancel any active alarm for this intention
        cancelReblockAlarm(intention.id)
        intentionDao.delete(intention)

        // Stop service if no more intentions and no active session
        if (intentionDao.getCount() == 0) {
            // Service will check if it should stop
            MonitoringService.checkAndStop(context)
        }
    }

    suspend fun openApp(intentionId: String) {
        val intention = intentionDao.getById(intentionId) ?: return
        intentionDao.incrementOpens(intentionId)
        intentionDao.setOpenState(intentionId, true, System.currentTimeMillis())
        scheduleReblockAlarm(intentionId, intention.timePerOpenMinutes)
    }

    suspend fun reblockApp(intentionId: String) {
        intentionDao.setOpenState(intentionId, false, null)
    }

    suspend fun getBlockedPackages(): Set<String> {
        return intentionDao.getBlockedIntentions()
            .map { it.packageName }
            .toSet()
    }

    private fun scheduleReblockAlarm(intentionId: String, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)

        // Schedule 30-second warning
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
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime - 30_000, null),
            warningPending
        )

        // Schedule actual re-block
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
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, null),
            reblockPending
        )
    }

    private fun cancelReblockAlarm(intentionId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val warningIntent = Intent(context, IntentionAlarmReceiver::class.java)
        val warningPending = PendingIntent.getBroadcast(
            context, intentionId.hashCode() + 1, warningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(warningPending)

        val reblockIntent = Intent(context, IntentionAlarmReceiver::class.java)
        val reblockPending = PendingIntent.getBroadcast(
            context, intentionId.hashCode(), reblockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reblockPending)
    }

    private fun ensureMonitoringServiceRunning() {
        MonitoringService.start(context)
    }
}
