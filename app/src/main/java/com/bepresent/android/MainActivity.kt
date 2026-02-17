package com.bepresent.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.ui.dashboard.DashboardScreen
import com.bepresent.android.ui.dashboard.DashboardViewModel
import com.bepresent.android.ui.dev.DevScreen
import com.bepresent.android.ui.leaderboard.LeaderboardScreen
import com.bepresent.android.ui.onboarding.OnboardingScreen
import com.bepresent.android.ui.partner.PartnerScreen
import com.bepresent.android.ui.profile.ProfileScreen
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
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            val viewModel: DashboardViewModel = hiltViewModel()
                            DashboardScreen(
                                viewModel = viewModel,
                                onProfileClick = { navController.navigate("profile") },
                                onLeaderboardClick = { navController.navigate("leaderboard") },
                                onDevClick = { navController.navigate("dev") }
                            )
                        }
                        composable("dev") {
                            DevScreen(onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onPartnerClick = { partnerId ->
                                    navController.navigate("partner/$partnerId")
                                }
                            )
                        }
                        composable("leaderboard") {
                            LeaderboardScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "partner/{partnerId}",
                            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
                        ) {
                            PartnerScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
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
