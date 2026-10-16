package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout

@Composable
fun MeasurementScreen(
    siteName: String,
    onSiteNameChange: (String) -> Unit,
    workspaceName: String,
    onWorkspaceNameChange: (String) -> Unit,
    onStartMeasurement: (String, String) -> Unit,
    onHistoryClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedName = normalizeWorkspaceName(workspaceName)
    val normalizedSite = normalizeSiteName(siteName)

    FeatureLayout("Measurement", "Scan markers or review previous measurements", onBack, modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("01 · New measurement", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = siteName,
                onValueChange = { updated ->
                    if (updated.length <= MAX_WORKSPACE_NAME_LENGTH) {
                        onSiteNameChange(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Site name") },
                supportingText = { Text("Required for database storage (max 255 characters).") },
                singleLine = true
            )

            OutlinedTextField(
                value = workspaceName,
                onValueChange = { updated ->
                    if (updated.length <= MAX_WORKSPACE_NAME_LENGTH) {
                        onWorkspaceNameChange(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cell / workspace name") },
                supportingText = {
                    Text("Required before starting a measurement (max 255 characters).")
                },
                singleLine = true
            )

            Button(
                onClick = {
                    if (normalizedSite != null && normalizedName != null) {
                        onStartMeasurement(normalizedSite, normalizedName)
                    }
                },
                enabled = normalizedSite != null && normalizedName != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Measurement")
            }
        }

        ActionCard("02", "Measurement History", "View saved measurements", onHistoryClick)
    }
}
