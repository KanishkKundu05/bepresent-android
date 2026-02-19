package com.bepresent.android.ui.homev2.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bepresent.android.ui.homev2.HomeV2Tokens

private const val MIN_DURATION_MINUTES = 15
private const val MAX_DURATION_MINUTES = 120
private const val STEP_MINUTES = 15

private fun xpForDuration(minutes: Int): Int = minutes * 2

private fun formatGoalDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionGoalSheet(
    currentDurationMinutes: Int,
    currentBeastMode: Boolean,
    onDismiss: () -> Unit,
    onSetGoal: (durationMinutes: Int, beastMode: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var goal by remember { mutableIntStateOf(currentDurationMinutes) }
    var isBeastModeOn by remember { mutableStateOf(currentBeastMode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HomeV2Tokens.NeutralWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Duration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HomeV2Tokens.NeutralBlack
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Duration stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(HomeV2Tokens.Neutral200)
                        .clickable {
                            goal = (goal - STEP_MINUTES).coerceAtLeast(MIN_DURATION_MINUTES)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = HomeV2Tokens.NeutralBlack
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = formatGoalDuration(goal),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeV2Tokens.NeutralBlack
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Plus button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(HomeV2Tokens.Neutral200)
                        .clickable {
                            goal = (goal + STEP_MINUTES).coerceAtMost(MAX_DURATION_MINUTES)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = HomeV2Tokens.NeutralBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // XP projection chip
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HomeV2Tokens.YellowFill)
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = HomeV2Tokens.YellowPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${xpForDuration(goal)} XP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeV2Tokens.YellowPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beast mode row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HomeV2Tokens.BrandPrimary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Beast Mode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = HomeV2Tokens.NeutralBlack
                    )
                }
                Switch(
                    checked = isBeastModeOn,
                    onCheckedChange = { isBeastModeOn = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = HomeV2Tokens.BrandPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Set Duration button
            FullButton(
                title = "Set Duration",
                appearance = FullButtonAppearance.Primary,
                onClick = { onSetGoal(goal, isBeastModeOn) }
            )
        }
    }
}
