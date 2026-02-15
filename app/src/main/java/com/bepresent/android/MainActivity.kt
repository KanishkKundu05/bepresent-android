package com.bepresent.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.ui.dashboard.DashboardScreen
import com.bepresent.android.ui.dashboard.DashboardViewModel
import com.bepresent.android.ui.onboarding.OnboardingScreen
import com.bepresent.android.ui.theme.BePresentTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BePresentTheme {
                val onboardingCompleted by preferencesManager.onboardingCompleted.collectAsState(initial = false)

                if (onboardingCompleted) {
                    val viewModel: DashboardViewModel = hiltViewModel()
                    DashboardScreen(viewModel = viewModel)
                } else {
                    OnboardingScreen(
                        onComplete = {
                            // Onboarding screen handles setting the flag
                        }
                    )
                }
            }
        }
    }
}
