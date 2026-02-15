package com.bepresent.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BePresentApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        com.bepresent.android.features.intentions.DailyResetWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val monitoringChannel = NotificationChannel(
            CHANNEL_MONITORING,
            "Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification while BePresent is monitoring app usage"
            setShowBadge(false)
        }

        val sessionChannel = NotificationChannel(
            CHANNEL_SESSION,
            "Focus Sessions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Session timer and goal notifications"
        }

        val intentionChannel = NotificationChannel(
            CHANNEL_INTENTION,
            "App Intentions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Timed open window notifications"
        }

        manager.createNotificationChannels(
            listOf(monitoringChannel, sessionChannel, intentionChannel)
        )
    }

    companion object {
        const val CHANNEL_MONITORING = "monitoring"
        const val CHANNEL_SESSION = "session"
        const val CHANNEL_INTENTION = "intention"
    }
}
