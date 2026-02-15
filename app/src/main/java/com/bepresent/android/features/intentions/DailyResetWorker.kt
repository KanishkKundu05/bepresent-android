package com.bepresent.android.features.intentions

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.data.db.AppIntentionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val intentionDao: AppIntentionDao,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString()
        val intentions = intentionDao.getAllOnce()

        val freezeAvailable = preferencesManager.getStreakFreezeAvailableOnce()
        var freezeUsed = false

        for (intention in intentions) {
            // Skip if already reset today
            if (intention.lastResetDate == today) continue

            val withinLimit = intention.totalOpensToday <= intention.allowedOpensPerDay
            val newStreak: Int

            if (withinLimit) {
                newStreak = intention.streak + 1
            } else if (freezeAvailable && !freezeUsed) {
                newStreak = intention.streak + 1
                freezeUsed = true
            } else {
                newStreak = 0
            }

            val updated = intention.copy(
                streak = newStreak,
                totalOpensToday = 0,
                lastResetDate = today,
                currentlyOpen = false,
                openedAt = null
            )
            intentionDao.upsert(updated)
        }

        // Consume freeze if used
        if (freezeUsed) {
            preferencesManager.setStreakFreezeAvailable(false)
        }

        // Grant new freeze on Mondays
        val dayOfWeek = LocalDate.now().dayOfWeek
        if (dayOfWeek == DayOfWeek.MONDAY) {
            val lastGrantDate = preferencesManager.getLastFreezeGrantDateOnce()
            if (lastGrantDate != today) {
                preferencesManager.setStreakFreezeAvailable(true)
                preferencesManager.setLastFreezeGrantDate(today)
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_reset"

        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            val nextMidnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
            val initialDelay = ChronoUnit.MILLIS.between(now, nextMidnight)

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
