package com.bepresent.android.features.blocking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bepresent.android.data.db.AppIntentionDao
import com.bepresent.android.data.db.PresentSessionDao
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.features.intentions.IntentionManager
import com.bepresent.android.features.sessions.SessionManager
import com.bepresent.android.ui.theme.BePresentTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlockedAppActivity : ComponentActivity() {

    @Inject lateinit var intentionManager: IntentionManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var intentionDao: AppIntentionDao
    @Inject lateinit var sessionDao: PresentSessionDao
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateHome()
            }
        })

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: run {
            finish()
            return
        }
        val shieldType = intent.getStringExtra(EXTRA_SHIELD_TYPE) ?: SHIELD_INTENTION

        setContent {
            BePresentTheme {
                ShieldScreen(
                    blockedPackage = blockedPackage,
                    shieldType = shieldType,
                    intentionManager = intentionManager,
                    sessionManager = sessionManager,
                    intentionDao = intentionDao,
                    sessionDao = sessionDao,
                    preferencesManager = preferencesManager,
                    onNavigateHome = { navigateHome() },
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun navigateHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        const val EXTRA_SHIELD_TYPE = "shield_type"
        const val SHIELD_SESSION = "session"
        const val SHIELD_INTENTION = "intention"
        const val SHIELD_GOAL_REACHED = "goalReached"
    }
}
