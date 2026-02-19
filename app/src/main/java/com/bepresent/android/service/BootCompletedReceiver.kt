package com.bepresent.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bepresent.android.debug.RuntimeLog
import com.bepresent.android.data.db.BePresentDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        RuntimeLog.i(TAG, "onReceive: BOOT_COMPLETED")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if there are active intentions or sessions
                val db = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    BePresentDatabase::class.java,
                    "bepresent.db"
                ).fallbackToDestructiveMigration().build()

                val hasIntentions = db.appIntentionDao().getCount() > 0
                val hasActiveSession = db.presentSessionDao().getActiveSession() != null
                db.close()
                RuntimeLog.i(TAG, "boot check: intentions=$hasIntentions activeSession=$hasActiveSession")

                if (hasIntentions || hasActiveSession) {
                    RuntimeLog.i(TAG, "boot check: starting MonitoringService")
                    MonitoringService.start(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BP_Boot"
    }
}
