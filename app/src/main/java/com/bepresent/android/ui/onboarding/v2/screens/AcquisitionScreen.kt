package com.bepresent.android.ui.onboarding.v2.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bepresent.android.ui.onboarding.v2.OnboardingTokens
import com.bepresent.android.ui.onboarding.v2.OnboardingTypography
import com.bepresent.android.ui.onboarding.v2.components.SurveyListItem

private val ACQUISITION_OPTIONS = listOf(
    "Recommended by a Friend" to "\uD83D\uDC64",
    "Facebook" to "\uD83D\uDFE6",
    "TikTok" to "\uD83C\uDFB5",
    "Instagram" to "\uD83D\uDCF7",
    "Reddit" to "\uD83E\uDD16",
    "App Store" to "\uD83D\uDED2",
    "Other" to null
)

@Composable
fun AcquisitionScreen(
    onSelect: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = OnboardingTokens.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "How did you hear\nabout BePresent?",
            style = OnboardingTypography.h2,
            color = OnboardingTokens.NeutralBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ACQUISITION_OPTIONS.forEach { (title, emoji) ->
                SurveyListItem(
                    title = title,
                    emoji = emoji,
                    isSelected = selectedOption == title,
                    enabled = !isProcessing,
                    onClick = {
                        selectedOption = title
                        isProcessing = true
                        onSelect(title)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
