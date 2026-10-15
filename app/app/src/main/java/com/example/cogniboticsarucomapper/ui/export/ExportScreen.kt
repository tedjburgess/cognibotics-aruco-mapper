package com.example.cogniboticsarucomapper.ui.export

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel
import com.google.gson.GsonBuilder

@Composable
fun ExportScreen(
    scans: List<ScanResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedScanId by remember {
        mutableStateOf<String?>(null)
    }

    val selectedScan = scans.firstOrNull {
        it.id == selectedScanId
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && selectedScan != null) {
            try {
                val gson = GsonBuilder()
                    .setPrettyPrinting()
                    .create()

                val json = gson.toJson(selectedScan)

                context.contentResolver
                    .openOutputStream(uri)
                    ?.bufferedWriter()
                    ?.use { writer ->
                        writer.write(json)
                    }

                Toast.makeText(
                    context,
                    "Measurement exported successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    FeatureLayout(
        title = "Export",
        subtitle = "Export saved measurement data",
        onBack = onBack,
        modifier = modifier
    ) {
        when {
            isLoading -> {
                InfoPanel(
                    title = "Loading measurements...",
                    description = "Fetching saved scans from the API."
                )
            }

            errorMessage != null -> {
                InfoPanel(
                    title = "Could not load measurements",
                    description = errorMessage
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retry")
                }
            }

            scans.isEmpty() -> {
                InfoPanel(
                    title = "No measurements available",
                    description = "Save a measurement before exporting data."
                )
            }

            else -> {
                Text(
                    text = "Select measurement",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Choose one saved scan to export as JSON.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                scans.forEach { scan ->
                    ScanSelectionCard(
                        scan = scan,
                        selected = scan.id == selectedScanId,
                        onSelect = {
                            selectedScanId = scan.id
                        }
                    )
                }

                if (selectedScan != null) {
                    HorizontalDivider()

                    Text(
                        text = "Export summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExportSummary(selectedScan)

                    Button(
                        onClick = {
                            saveLauncher.launch(
                                createExportFileName(selectedScan)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export measurement as JSON")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSelectionCard(scan: ScanResponse, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${scan.site_name} / ${scan.cell_name}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = scan.measured_at,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${scan.observations.size} markers · ${scan.relations.size} relations",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ExportSummary(scan: ScanResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SummaryRow("Site", scan.site_name)
        SummaryRow("Cell / workspace", scan.cell_name)
        SummaryRow("Device", scan.device_id)
        SummaryRow("Measured at", scan.measured_at)
        SummaryRow("Markers", scan.observations.size.toString())
        SummaryRow("Relations", scan.relations.size.toString())

        Text(
            text = "The JSON file contains the complete marker observations, positions, " +
                "orientations, physical sizes and relations returned by the API.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun createExportFileName(scan: ScanResponse): String {
    val workspace = scan.cell_name
        .replace(Regex("[^A-Za-z0-9_-]"), "_")

    val timestamp = scan.measured_at
        .replace(":", "-")
        .replace(Regex("[^A-Za-z0-9_-]"), "_")

    return "${workspace}_$timestamp.json"
}
