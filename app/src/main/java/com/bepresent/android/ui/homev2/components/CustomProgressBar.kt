package com.bepresent.android.ui.homev2.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.2f),
    filledColor: Color = Color.Blue,
    height: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .height(height)
                .clip(CircleShape)
                .background(filledColor)
        )
    }
}
