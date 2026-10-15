package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.network.model.ObservationResponse
import com.example.cogniboticsarucomapper.network.model.RelationResponse
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.ui.components.InfoPanel
import com.example.cogniboticsarucomapper.ui.components.PageHeading
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
    scans: List<ScanResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        PageHeading(
            "Measurement History",
            "Saved measurements and complete scan details",
            onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    InfoPanel(
                        "Loading measurements...",
                        "Fetching saved scans from the API."
                    )
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InfoPanel(
                            "Could not load measurements",
                            errorMessage
                        )

                        Button(
                            onClick = onRetry
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            scans.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    InfoPanel(
                        "No measurements saved yet.",
                        "Saved measurements will appear here."
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = scans,
                        key = { scan -> scan.id }
                    ) { scan ->
                        MeasurementCard(scan)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(scan: ScanResponse) {
    var expanded by rememberSaveable(scan.id) {
        mutableStateOf(false)
    }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${scan.site_name} / ${scan.cell_name}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = scan.measured_at,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${scan.observations.size} markers · ${scan.relations.size} relations",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (expanded) "Tap to hide details" else "Tap to show full details",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                ScanDetails(scan)
            }
        }
    }
}

@Composable
private fun ScanDetails(scan: ScanResponse) {
    Column(
        modifier = Modifier.padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailSection("Scan information") {
            DetailRow("Scan ID", scan.id)
            DetailRow("Site", scan.site_name)
            DetailRow("Cell / workspace", scan.cell_name)
            DetailRow("Device ID", scan.device_id)
            DetailRow("Measured at", scan.measured_at)
            DetailRow("Created at", scan.created_at)
        }

        HorizontalDivider()

        Text(
            text = "Markers (${scan.observations.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (scan.observations.isEmpty()) {
            Text(
                "No marker observations.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            scan.observations.forEachIndexed { index, observation ->
                ObservationDetails(
                    number = index + 1,
                    observation = observation
                )
            }
        }

        HorizontalDivider()

        Text(
            text = "Relations (${scan.relations.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (scan.relations.isEmpty()) {
            Text(
                "No marker relations.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            val markersById = scan.observations.associateBy {
                it.marker_id
            }

            scan.relations.forEachIndexed { index, relation ->
                RelationDetails(
                    number = index + 1,
                    relation = relation,
                    fromMarker = markersById[relation.from_marker_id],
                    toMarker = markersById[relation.to_marker_id]
                )
            }
        }
    }
}

@Composable
private fun ObservationDetails(number: Int, observation: ObservationResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Marker $number — ${markerLabel(observation)}",
            style = MaterialTheme.typography.titleSmall
        )

        DetailRow("Observation ID", observation.id)
        DetailRow("Marker UUID", observation.marker_id)
        DetailRow("ArUco ID", observation.aruco_id.toString())
        DetailRow("Dictionary", observation.dictionary)
        DetailRow("Name", observation.name ?: "Not set")
        DetailRow(
            "Physical size",
            observation.size_mm?.let { "${formatNumber(it)} mm" } ?: "Not available"
        )

        Text(
            "Position",
            style = MaterialTheme.typography.labelLarge
        )

        DetailRow("X", formatNumber(observation.position.x))
        DetailRow("Y", formatNumber(observation.position.y))
        DetailRow("Z", formatNumber(observation.position.z))

        Text(
            "Orientation quaternion",
            style = MaterialTheme.typography.labelLarge
        )

        DetailRow("X", formatNumber(observation.orientation.x))
        DetailRow("Y", formatNumber(observation.orientation.y))
        DetailRow("Z", formatNumber(observation.orientation.z))
        DetailRow("W", formatNumber(observation.orientation.w))
    }
}

@Composable
private fun RelationDetails(
    number: Int,
    relation: RelationResponse,
    fromMarker: ObservationResponse?,
    toMarker: ObservationResponse?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Relation $number",
            style = MaterialTheme.typography.titleSmall
        )

        DetailRow("Relation ID", relation.id)

        DetailRow(
            "From",
            fromMarker?.let { markerLabel(it) }
                ?: relation.from_marker_id
        )

        DetailRow(
            "To",
            toMarker?.let { markerLabel(it) }
                ?: relation.to_marker_id
        )

        DetailRow(
            "Distance",
            "${formatNumber(relation.distance_mm)} mm"
        )

        relation.relative_position?.let { position ->
            Text(
                "Relative position",
                style = MaterialTheme.typography.labelLarge
            )

            DetailRow("X", formatNumber(position.x))
            DetailRow("Y", formatNumber(position.y))
            DetailRow("Z", formatNumber(position.z))
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun markerLabel(observation: ObservationResponse): String =
    "Marker ${observation.aruco_id} (${observation.dictionary})"

private fun formatNumber(value: Double): String = String.format(
    Locale.getDefault(),
    "%.3f",
    value
)
