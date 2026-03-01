package com.bepresent.android.ui.homev2.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.ui.homev2.HomeV2Tokens

data class DayUiModel(
    val weekDay: String,
    val number: String,
    val isEnabled: Boolean,
    val isChecked: Boolean,
    val isCurrentDay: Boolean
)

// Arc transform arrays matching iOS exactly
private val arcHorizontalPadding = listOf(6, 5, 2, 2, 2, 5, 6)
private val arcYOffset = listOf(40, 15, 0, 0, 0, 15, 40)
private val arcRotation = listOf(-18f, -15f, -10f, 0f, 10f, 15f, 18f)

@Composable
fun HomeDateCarousel(
    days: List<DayUiModel>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, day ->
            CalendarDayCell(
                day = day,
                modifier = Modifier
                    .padding(horizontal = arcHorizontalPadding.getOrElse(index) { 0 }.dp)
                    .offset(y = arcYOffset.getOrElse(index) { 0 }.dp)
                    .rotate(arcRotation.getOrElse(index) { 0f })
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: DayUiModel,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        day.isCurrentDay -> HomeV2Tokens.NeutralWhite
        day.isEnabled -> HomeV2Tokens.NeutralWhite.copy(alpha = 0.25f)
        else -> HomeV2Tokens.NeutralWhite.copy(alpha = 0.15f)
    }

    val textColor = when {
        day.isCurrentDay -> HomeV2Tokens.NeutralBlack
        day.isEnabled -> HomeV2Tokens.NeutralWhite
        else -> HomeV2Tokens.NeutralWhite.copy(alpha = 0.5f)
    }

    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.weekDay,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = day.number,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Status indicator
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                day.isChecked && day.isEnabled -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(HomeV2Tokens.GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                    }
                }
                day.isEnabled -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (day.isCurrentDay) HomeV2Tokens.Neutral200
                                else HomeV2Tokens.NeutralWhite.copy(alpha = 0.3f)
                            )
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(HomeV2Tokens.NeutralWhite.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}
