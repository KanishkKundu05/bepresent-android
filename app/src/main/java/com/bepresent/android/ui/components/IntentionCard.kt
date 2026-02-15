package com.bepresent.android.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bepresent.android.data.db.AppIntention

@Composable
fun IntentionCard(
    intention: AppIntention,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val appIcon = remember(intention.packageName) {
        try {
            pm.getApplicationIcon(intention.packageName).toBitmap(64, 64).asImageBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = intention.appName,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = intention.appName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${intention.totalOpensToday}/${intention.allowedOpensPerDay}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (intention.totalOpensToday >= intention.allowedOpensPerDay) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (intention.streak > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\uD83D\uDD25 ${intention.streak}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
