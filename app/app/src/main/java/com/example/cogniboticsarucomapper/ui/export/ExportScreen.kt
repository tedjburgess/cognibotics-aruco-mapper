package com.example.cogniboticsarucomapper.ui.export

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
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel

@Composable
fun ExportScreen(
    state: ExportUiState,
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    onSave: () -> Unit,
    onStartMeasurement: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scan = state.scan
    FeatureLayout(
        title = "Export",
        subtitle = "Save a captured measurement to the API database",
        onBack = onBack,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            label = { Text("API server URL") },
            supportingText = { Text("Emulator: http://10.0.2.2:8000/; phone: use your PC's LAN IP.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        )
        if (scan == null || scan.observations.isEmpty()) {
            InfoPanel(
                "No measurement captured",
                "Start a live measurement and capture a marker before saving it."
            )
            Button(onClick = onStartMeasurement, modifier = Modifier.fillMaxWidth()) {
                Text("Start a measurement")
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Measurement summary", style = MaterialTheme.typography.titleMedium)
                SummaryRow("Site", scan.site_name)
                SummaryRow("Cell / workspace", scan.cell_name)
                SummaryRow("Device", scan.device_id)
                SummaryRow("Measured at (UTC)", scan.measured_at)
                SummaryRow("Markers", scan.observations.size.toString())
                SummaryRow("Relations", scan.relations.size.toString())
                Text(
                    "Saving sends the captured positions, orientations and marker relations " +
                        "to the API. It does not download a local file.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.savedScanId != null) {
                InfoPanel(
                    "Measurement saved successfully",
                    "The database confirmed scan ID: ${state.savedScanId}"
                )
            } else {
                if (state.errorMessage != null) {
                    InfoPanel("Could not save measurement", state.errorMessage)
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save measurement to database")
                }
            }

            Button(
                onClick = onStartMeasurement,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capture another measurement")
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
}
