package com.bepresent.android.ui.onboarding.v2.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bepresent.android.ui.onboarding.v2.OnboardingTokens

@Composable
fun OnboardingProgressBar(
    currentStep: Float,
    totalSteps: Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (totalSteps > 0) (currentStep / totalSteps).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "progress"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = OnboardingTokens.NeutralBlack,
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(OnboardingTokens.ProgressBarHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(OnboardingTokens.Neutral200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(OnboardingTokens.ProgressBarHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(OnboardingTokens.Neutral900)
            )
        }
    }
}
