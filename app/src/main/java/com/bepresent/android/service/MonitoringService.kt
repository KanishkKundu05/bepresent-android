package com.bepresent.android.service

import android.app.Notification
import android.util.Log
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bepresent.android.BePresentApp
import com.bepresent.android.MainActivity
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.data.db.PresentSession
import com.bepresent.android.data.db.PresentSessionDao
import com.bepresent.android.data.usage.UsageStatsRepository
import com.bepresent.android.features.blocking.BlockedAppActivity
import com.bepresent.android.features.sessions.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject lateinit var usageStatsRepository: UsageStatsRepository
    @Inject lateinit var intentionDao: AppIntentionDao
    @Inject lateinit var sessionDao: PresentSessionDao
    @Inject lateinit var sessionManager: SessionManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var pollingJob: Job? = null
    private var lastBlockedPackage: String? = null
    private var lastBlockedTime: Long = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMonitoringNotification())
        startPolling()
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val foregroundPackage = usageStatsRepository.detectForegroundApp()
                    if (foregroundPackage != null && foregroundPackage != packageName) {
                        val blockedPackages = getBlockedPackages()
                        if (foregroundPackage in blockedPackages) {
                            // Debounce: don't re-launch shield for same app within 2 seconds
                            val now = System.currentTimeMillis()
                            if (foregroundPackage != lastBlockedPackage || now - lastBlockedTime > 2000) {
                                lastBlockedPackage = foregroundPackage
                                lastBlockedTime = now
                                val shieldType = determineShieldType(foregroundPackage)
                                launchBlockedActivity(foregroundPackage, shieldType)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MonitoringService", "Polling error", e)
                }
                delay(1000)
            }
        }
    }

    private suspend fun getBlockedPackages(): Set<String> {
        val sessionBlocked = sessionDao.getActiveSession()?.let { session ->
            sessionManager.getBlockedPackagesFromJson(session.blockedPackages)
        } ?: emptySet()

        val intentionBlocked = intentionDao.getBlockedIntentions()
            .map { it.packageName }
            .toSet()

        return sessionBlocked + intentionBlocked
    }

    private suspend fun determineShieldType(packageName: String): String {
        // Session takes priority over intention
        val activeSession = sessionDao.getActiveSession()
        if (activeSession != null) {
            val sessionPackages = sessionManager.getBlockedPackagesFromJson(activeSession.blockedPackages)
            if (packageName in sessionPackages) {
                return if (activeSession.state == PresentSession.STATE_GOAL_REACHED) {
                    BlockedAppActivity.SHIELD_GOAL_REACHED
                } else {
                    BlockedAppActivity.SHIELD_SESSION
                }
            }
        }
        return BlockedAppActivity.SHIELD_INTENTION
    }

    private fun launchBlockedActivity(packageName: String, shieldType: String) {
        val intent = Intent(this, BlockedAppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(BlockedAppActivity.EXTRA_SHIELD_TYPE, shieldType)
        }
        startActivity(intent)
    }

    private fun createMonitoringNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, BePresentApp.CHANNEL_MONITORING)
            .setContentTitle("BePresent is active")
            .setContentText("Monitoring your app usage")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateNotificationForSession(session: PresentSession) {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = if (session.state == PresentSession.STATE_GOAL_REACHED) {
            val (xp, _) = com.bepresent.android.features.sessions.SessionStateMachine.calculateRewards(session.goalDurationMinutes)
            NotificationCompat.Builder(this, BePresentApp.CHANNEL_SESSION)
                .setContentTitle("Goal Reached!")
                .setContentText("+$xp XP — tap to complete")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        } else {
            val elapsed = System.currentTimeMillis() - (session.startedAt ?: System.currentTimeMillis())
            val remaining = (session.goalDurationMinutes * 60 * 1000L) - elapsed
            val remainingMinutes = (remaining / 60000).coerceAtLeast(0)

            NotificationCompat.Builder(this, BePresentApp.CHANNEL_SESSION)
                .setContentTitle(session.name)
                .setContentText("${remainingMinutes}m remaining")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(System.currentTimeMillis() + remaining)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitoringService::class.java))
        }

        fun checkAndStop(context: Context, sessionDao: PresentSessionDao, intentionDao: AppIntentionDao) {
            CoroutineScope(Dispatchers.IO).launch {
                val hasActiveSession = sessionDao.getActiveSession() != null
                val intentionCount = intentionDao.getCount()
                if (!hasActiveSession && intentionCount == 0) {
                    stop(context)
                }
            }
        }
    }
}
