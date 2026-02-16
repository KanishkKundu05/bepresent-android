package com.bepresent.android.ui.partner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bepresent.android.data.convex.ConvexManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartnerSheet(
    convexManager: ConvexManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var friendCode by remember { mutableStateOf("") }
    var foundUser by remember { mutableStateOf<Pair<String, String>?>(null) } // (userId, displayName)
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var requestSent by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Partner", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = friendCode,
                onValueChange = {
                    friendCode = it.uppercase().take(6)
                    foundUser = null
                    error = null
                    requestSent = false
                },
                label = { Text("Friend Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (foundUser != null && !requestSent) {
                Text(
                    "Found: ${foundUser!!.second}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                convexManager.client.mutation<Unit>(
                                    "partners:sendRequest",
                                    args = mapOf("partnerId" to foundUser!!.first)
                                )
                                requestSent = true
                            } catch (e: Exception) {
                                error = e.message ?: "Failed to send request"
                            }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Send Request")
                }
            } else if (requestSent) {
                Text(
                    "Request sent!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                val result = convexManager.client.subscribe<Map<String, Any?>>(
                                    "partners:lookupByFriendCode",
                                    args = mapOf("code" to friendCode)
                                )
                                // Collect first result from subscription
                                result.collect { r ->
                                    r.onSuccess { data ->
                                        if (data != null) {
                                            foundUser = Pair(
                                                data["userId"] as String,
                                                data["displayName"] as String
                                            )
                                        } else {
                                            error = "No user found with that code"
                                        }
                                    }
                                    r.onFailure {
                                        error = "Lookup failed"
                                    }
                                    isLoading = false
                                    return@collect
                                }
                            } catch (e: Exception) {
                                error = e.message ?: "Lookup failed"
                                isLoading = false
                            }
                        }
                    },
                    enabled = friendCode.length == 6 && !isLoading
                ) {
                    Text("Look Up")
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
