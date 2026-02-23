package com.bepresent.android.ui.onboarding.v2.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bepresent.android.R
import com.bepresent.android.ui.onboarding.v2.OnboardingTokens
import com.bepresent.android.ui.onboarding.v2.OnboardingTypography
import com.bepresent.android.ui.onboarding.v2.components.LaurelBadge
import com.bepresent.android.ui.onboarding.v2.components.OnboardingContinueButton
import com.bepresent.android.ui.onboarding.v2.components.OnboardingButtonAppearance
import com.bepresent.android.ui.onboarding.v2.components.ReviewCard
import com.bepresent.android.ui.onboarding.v2.components.ReviewData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserWhatScreen(onContinue: () -> Unit) {
    val reviews = remember {
        listOf(
            ReviewData(
                user = "Sarah M.",
                title = "Actually works!",
                body = "I've tried every screen time app out there. BePresent is the only one that actually helped me cut down. The streaks keep me motivated!"
            ),
            ReviewData(
                user = "James K.",
                title = "Game changer",
                body = "Went from 8 hours daily screen time to 3. The leaderboard with my friends makes it fun instead of feeling like a chore."
            ),
            ReviewData(
                user = "Emily R.",
                title = "Best investment",
                body = "I'm reading more, sleeping better, and actually present with my family. This app gave me my evenings back."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { reviews.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = OnboardingTokens.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Title
        Text(
            text = "567,000+ People\nAchieved Their\nGoals with\nBePresent",
            style = OnboardingTypography.title2,
            color = OnboardingTokens.NeutralBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stars with laurels
        LaurelBadge {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = OnboardingTokens.YellowPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "22,139+",
                    style = OnboardingTypography.h1,
                    color = OnboardingTokens.NeutralBlack
                )
                Text(
                    text = "5-Star Reviews",
                    style = OnboardingTypography.p2,
                    color = OnboardingTokens.NeutralBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Review carousel
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            ReviewCard(review = reviews[page])
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continue button
        OnboardingContinueButton(
            title = "Continue",
            appearance = OnboardingButtonAppearance.Secondary,
            onClick = onContinue
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
