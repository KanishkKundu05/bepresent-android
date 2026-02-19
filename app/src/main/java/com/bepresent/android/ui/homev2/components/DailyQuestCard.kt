package com.bepresent.android.ui.homev2.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.ui.homev2.HomeV2Tokens

data class DailyQuestUiState(
    val completedReview: Boolean = false,
    val completedTip: Boolean = false,
    val completedSession: Boolean = false
) {
    val completedCount: Int
        get() = listOf(completedReview, completedTip, completedSession).count { it }
}

@Composable
fun DailyQuestCard(
    state: DailyQuestUiState,
    onReviewClick: () -> Unit = {},
    onTipClick: () -> Unit = {},
    onSessionClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Header + progress count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Quest",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeV2Tokens.NeutralBlack
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${state.completedCount}/3 complete",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = HomeV2Tokens.Brand300
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        CustomProgressBar(
            progress = state.completedCount / 3f,
            backgroundColor = HomeV2Tokens.Brand300.copy(alpha = 0.1f),
            filledColor = HomeV2Tokens.Brand300,
            height = 8.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist rows
        QuestRow(
            title = "Yesterday's Review",
            isCompleted = state.completedReview,
            onClick = onReviewClick
        )
        QuestDivider()

        QuestRow(
            title = "Tip of the Day",
            isCompleted = state.completedTip,
            onClick = onTipClick
        )
        QuestDivider()

        QuestRow(
            title = "Present Session",
            isCompleted = state.completedSession,
            onClick = onSessionClick
        )
        QuestDivider()

        // Rewards row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRewardsClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(HomeV2Tokens.YellowPrimary.copy(alpha = 0.5f))
                    .padding(4.dp),
                tint = HomeV2Tokens.NeutralBlack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Rewards",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HomeV2Tokens.NeutralBlack
            )
        }
    }
}

@Composable
private fun QuestRow(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (isCompleted) HomeV2Tokens.GreenPrimary else HomeV2Tokens.Neutral200
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isCompleted) Color.Gray else HomeV2Tokens.NeutralBlack
        )
    }
}

@Composable
private fun QuestDivider() {
    Divider(
        color = HomeV2Tokens.Neutral200,
        thickness = 0.5.dp
    )
}
