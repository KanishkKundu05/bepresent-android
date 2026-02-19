package com.bepresent.android.ui.homev2.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.ui.homev2.HomeV2Tokens
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Segmented control style picker matching iOS CustomPickerView.
 */
@Composable
fun CustomPickerView(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(HomeV2Tokens.Neutral200)
            .onSizeChanged { rowSize = it }
            .padding(4.dp)
    ) {
        // Sliding indicator
        if (rowSize.width > 0 && options.isNotEmpty()) {
            val segmentWidth = with(density) { ((rowSize.width - 8.dp.toPx()) / options.size).toDp() }
            val offsetX by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = tween(200),
                label = "pickerSlide"
            )

            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .height(36.dp)
                    .then(
                        with(density) {
                            Modifier.padding(end = 0.dp)
                        }
                    )
                    .clip(CircleShape)
                    .background(HomeV2Tokens.NeutralWhite)
                    .then(
                        Modifier.fillMaxWidth(1f / options.size)
                    )
            )
        }

        // Labels
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectionChanged(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) HomeV2Tokens.NeutralBlack else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
