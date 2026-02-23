package com.bepresent.android.ui.onboarding.v2.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bepresent.android.ui.onboarding.v2.components.OnboardingContinueButton
import com.bepresent.android.ui.onboarding.v2.components.OnboardingButtonAppearance
import com.bepresent.android.ui.picker.AppPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerOnboardingScreen(
    onComplete: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var selectedCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = OnboardingTokens.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        Text(
            text = "\uD83D\uDCF1",
            style = OnboardingTypography.extraLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Apps\nto Block",
            style = OnboardingTypography.h1,
            color = OnboardingTokens.NeutralBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose the apps that distract you the most. You can always change this later.",
            style = OnboardingTypography.p2,
            color = OnboardingTokens.Neutral800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        if (selectedCount > 0) {
            Text(
                text = "$selectedCount app${if (selectedCount != 1) "s" else ""} selected",
                style = OnboardingTypography.label,
                color = OnboardingTokens.BrandPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OnboardingContinueButton(
            title = if (selectedCount == 0) "Choose Apps" else "Continue",
            appearance = OnboardingButtonAppearance.Secondary,
            onClick = {
                if (selectedCount == 0) {
                    showPicker = true
                } else {
                    onComplete()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedCount == 0) {
            OnboardingContinueButton(
                title = "Skip for now",
                appearance = OnboardingButtonAppearance.Secondary,
                onClick = onComplete
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showPicker) {
        AppPickerSheet(
            multiSelect = true,
            onDismiss = { showPicker = false },
            onAppsSelected = { apps ->
                selectedCount = apps.size
                showPicker = false
            }
        )
    }
}
