package com.example.cogniboticsarucomapper.ui.export

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel
import kotlinx.coroutines.launch

@Composable
fun ExportScreen(
    scan: ScanRequest?,
    savedScanId: String?,
    onSave: suspend (ScanRequest) -> ScanResponse,
    onSaved: (String) -> Unit,
    onStartMeasurement: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    FeatureLayout(
        title = "Export",
        subtitle = "Save a captured measurement to the API database",
        onBack = onBack,
        modifier = modifier
    ) {
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

            if (savedScanId != null) {
                InfoPanel(
                    "Measurement saved successfully",
                    "The database confirmed scan ID: $savedScanId"
                )
            } else {
                if (saveError != null) {
                    InfoPanel("Could not save measurement", requireNotNull(saveError))
                }
                Button(
                    onClick = {
                        if (!saving) {
                            saving = true
                            saveError = null
                            scope.launch {
                                try {
                                    val response = onSave(scan)
                                    onSaved(response.id)
                                } catch (error: Exception) {
                                    Log.e("ExportScreen", "Saving scan failed", error)
                                    saveError = "The API could not save this measurement. " +
                                        "Check the connection and retry."
                                } finally {
                                    saving = false
                                }
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Saving..." else "Save measurement to database")
                }
            }
            Button(onClick = onStartMeasurement, modifier = Modifier.fillMaxWidth()) {
                Text("Capture another measurement")
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
}
