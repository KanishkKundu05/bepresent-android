package com.bepresent.android.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bepresent.android.ui.picker.InstalledApp

private val DURATION_OPTIONS = listOf(5, 10, 15, 20, 30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SessionConfigSheet(
    onDismiss: () -> Unit,
    onOpenAppPicker: () -> Unit,
    selectedApps: List<InstalledApp>,
    onStart: (name: String, durationMinutes: Int, beastMode: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sessionName by remember { mutableStateOf("Focus Session") }
    var selectedDuration by remember { mutableIntStateOf(30) }
    var beastMode by remember { mutableStateOf(false) }

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
                text = "Start Blocking Session",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Session name
            OutlinedTextField(
                value = sessionName,
                onValueChange = { sessionName = it },
                label = { Text("Session Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Goal duration
            Text(
                text = "Goal Duration",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DURATION_OPTIONS.forEach { minutes ->
                    FilterChip(
                        selected = selectedDuration == minutes,
                        onClick = { selectedDuration = minutes },
                        label = {
                            Text(
                                if (minutes >= 60) "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}m" else ""}"
                                else "${minutes}m"
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Apps to block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apps to Block",
                    style = MaterialTheme.typography.titleSmall
                )
                TextButton(onClick = onOpenAppPicker) {
                    Text(
                        if (selectedApps.isEmpty()) "Select Apps"
                        else "${selectedApps.size} app${if (selectedApps.size > 1) "s" else ""} selected"
                    )
                }
            }

            if (selectedApps.isNotEmpty()) {
                Text(
                    text = selectedApps.joinToString(", ") { it.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beast mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Beast Mode",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Disable \"Give Up\" \u2014 you can't end early",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = beastMode,
                    onCheckedChange = { beastMode = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onStart(sessionName, selectedDuration, beastMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = selectedApps.isNotEmpty()
            ) {
                Text("Start Session", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
