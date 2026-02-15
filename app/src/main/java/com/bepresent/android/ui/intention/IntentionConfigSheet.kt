package com.bepresent.android.ui.intention

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bepresent.android.data.db.AppIntention

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentionConfigSheet(
    appName: String,
    existingIntention: AppIntention? = null,
    onDismiss: () -> Unit,
    onSave: (allowedOpensPerDay: Int, timePerOpenMinutes: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var allowedOpens by remember {
        mutableFloatStateOf((existingIntention?.allowedOpensPerDay ?: 3).toFloat())
    }
    var timePerOpen by remember {
        mutableFloatStateOf((existingIntention?.timePerOpenMinutes ?: 5).toFloat())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = if (existingIntention != null) "Edit Intention" else "Create Intention",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Allowed opens per day
            Text(
                text = "Allowed opens per day",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = allowedOpens,
                    onValueChange = { allowedOpens = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${allowedOpens.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time per open
            Text(
                text = "Time per open (minutes)",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = timePerOpen,
                    onValueChange = { timePerOpen = it },
                    valueRange = 1f..30f,
                    steps = 28,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${timePerOpen.toInt()}m",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(allowedOpens.toInt(), timePerOpen.toInt()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (existingIntention != null) "Save Changes" else "Create Intention",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Intention")
                }
            }
        }
    }
}
