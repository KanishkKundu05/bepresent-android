package com.bepresent.android.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bepresent.android.BePresentApp
import com.bepresent.android.debug.RuntimeLog
import com.bepresent.android.data.db.BePresentDatabase
import com.bepresent.android.data.usage.UsageStatsRepository
import com.bepresent.android.features.blocking.BlockedAppActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntentionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val intentionId = intent.getStringExtra(EXTRA_INTENTION_ID) ?: return
        RuntimeLog.i(TAG, "onReceive: action=${intent.action} intentionId=$intentionId")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    BePresentDatabase::class.java,
                    "bepresent.db"
                ).fallbackToDestructiveMigration().build()

                val dao = db.appIntentionDao()
                val intention = dao.getById(intentionId)

                when (intent.action) {
                    ACTION_WARNING -> {
                        if (intention != null && intention.currentlyOpen) {
                            RuntimeLog.i(TAG, "ACTION_WARNING: intention=${intention.id} app=${intention.appName}")
                            showNotification(
                                context,
                                "Closing ${intention.appName} in 30 seconds",
                                "Your open window is almost over",
                                intentionId.hashCode() + 100
                            )
                        } else {
                            RuntimeLog.d(TAG, "ACTION_WARNING skipped: intention missing or not open")
                        }
                    }
                    ACTION_REBLOCK -> {
                        if (intention != null) {
                            RuntimeLog.i(TAG, "ACTION_REBLOCK: intention=${intention.id} app=${intention.appName}")
                            dao.setOpenState(intentionId, false, null)
                            showNotification(
                                context,
                                "${intention.appName} time is up",
                                "App has been re-shielded",
                                intentionId.hashCode() + 100
                            )

                            // If the blocked app is still in the foreground, launch shield
                            val usageRepo = UsageStatsRepository(context.applicationContext)
                            val foregroundApp = usageRepo.detectForegroundApp()
                            if (foregroundApp == intention.packageName) {
                                val shieldIntent = Intent(context, BlockedAppActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, intention.packageName)
                                    putExtra(BlockedAppActivity.EXTRA_SHIELD_TYPE, BlockedAppActivity.SHIELD_INTENTION)
                                }
                                try {
                                    context.startActivity(shieldIntent)
                                    RuntimeLog.i(TAG, "ACTION_REBLOCK: launched shield for ${intention.packageName}")
                                } catch (t: Throwable) {
                                    RuntimeLog.e(TAG, "ACTION_REBLOCK: failed to launch shield", t)
                                }
                            } else {
                                RuntimeLog.d(
                                    TAG,
                                    "ACTION_REBLOCK: foreground=$foregroundApp does not match ${intention.packageName}"
                                )
                            }
                        } else {
                            RuntimeLog.w(TAG, "ACTION_REBLOCK skipped: intention missing for id=$intentionId")
                        }
                    }
                }

                db.close()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, text: String, notifId: Int) {
        val notification = NotificationCompat.Builder(context, BePresentApp.CHANNEL_INTENTION)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    companion object {
        private const val TAG = "BP_IntAlarm"
        const val ACTION_WARNING = "com.bepresent.android.INTENTION_WARNING"
        const val ACTION_REBLOCK = "com.bepresent.android.INTENTION_REBLOCK"
        const val EXTRA_INTENTION_ID = "intention_id"
    }
}
