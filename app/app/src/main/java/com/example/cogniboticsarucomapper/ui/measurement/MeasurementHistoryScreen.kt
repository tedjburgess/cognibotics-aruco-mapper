package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cogniboticsarucomapper.ui.components.InfoPanel
import com.example.cogniboticsarucomapper.ui.components.PageHeading
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MeasurementHistoryItem(
    val id: String,
    val siteName: String,
    val cellName: String,
    val measuredAt: String,
    val observationCount: Int,
    val distances: List<MarkerDistanceItem> = emptyList()
)

@Composable
fun MeasurementHistoryScreen(
    measurements: List<MeasurementHistoryItem>,
    onBack: () -> Unit,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        PageHeading("Measurement History", "Saved measurements", onBack)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    scope.launch {
                        refreshing = true
                        error = null
                        try {
                            onRefresh()
                        } catch (e: Exception) {
                            error = e.message ?: "Could not refresh measurements"
                        } finally {
                            refreshing = false
                        }
                    }
                },
                enabled = !refreshing
            ) {
                if (refreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                }
                Text(if (refreshing) "Refreshing" else "Refresh")
            }
        }

        if (error != null) {
            Text(
                text = "Could not refresh: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (measurements.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                InfoPanel("No measurements saved", "Saved measurements will appear here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(measurements, key = { it.id }) { measurement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${measurement.siteName} / ${measurement.cellName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                formatMeasurementTime(measurement.measuredAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${measurement.observationCount} ${if (measurement.observationCount == 1) "marker" else "markers"}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            HorizontalDivider()
                            Text("Measured distances", style = MaterialTheme.typography.titleSmall)
                            if (measurement.distances.isEmpty()) {
                                Text(
                                    "No distances recorded for this measurement.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                measurement.distances.forEach { distance ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${distance.fromLabel} to ${distance.toLabel}",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            distance.distanceMm?.let {
                                                String.format(Locale.getDefault(), "%.1f mm", it)
                                            } ?: "Unavailable",
                                            modifier = Modifier.padding(start = 8.dp),
                                            style = MaterialTheme.typography.titleSmall,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMeasurementTime(value: String): String = runCatching {
    OffsetDateTime.parse(value)
        .atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy, HH:mm z", Locale.getDefault()))
}.getOrDefault(value)
