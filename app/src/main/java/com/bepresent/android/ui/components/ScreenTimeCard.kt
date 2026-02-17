package com.bepresent.android.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bepresent.android.data.usage.AppUsageInfo

private const val MAX_SCREEN_TIME_MS = 8 * 60 * 60 * 1000L // 8 hours

@Composable
fun ScreenTimeCard(
    totalScreenTimeMs: Long,
    perAppUsage: List<AppUsageInfo>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Screen Time Today",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = (totalScreenTimeMs.toFloat() / MAX_SCREEN_TIME_MS).coerceIn(0f, 1f),
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDuration(totalScreenTimeMs),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (perAppUsage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(perAppUsage.take(10)) { app ->
                        val appLabel = remember(app.packageName) {
                            try {
                                pm.getApplicationLabel(
                                    pm.getApplicationInfo(app.packageName, 0)
                                ).toString()
                            } catch (_: PackageManager.NameNotFoundException) {
                                app.packageName.substringAfterLast(".")
                            }
                        }
                        val appIcon = remember(app.packageName) {
                            try {
                                pm.getApplicationIcon(app.packageName).toBitmap(48, 48).asImageBitmap()
                            } catch (_: Exception) {
                                null
                            }
                        }

                        SuggestionChip(
                            onClick = { },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (appIcon != null) {
                                        Image(
                                            bitmap = appIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "$appLabel ${formatDuration(app.totalTimeMs)}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
