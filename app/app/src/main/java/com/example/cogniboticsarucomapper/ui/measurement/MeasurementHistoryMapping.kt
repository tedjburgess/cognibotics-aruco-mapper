package com.example.cogniboticsarucomapper.ui.measurement

import com.example.cogniboticsarucomapper.network.model.ObservationResponse
import com.example.cogniboticsarucomapper.network.model.ScanResponse

data class MarkerDistanceItem(
    val id: String,
    val fromLabel: String,
    val toLabel: String,
    val distanceMm: Double?
)

/** Preserve the backend's distances; do not infer a new distance from screen coordinates. */
fun ScanResponse.toMeasurementHistoryItem(): MeasurementHistoryItem {
    val markers = observations.associateBy { it.marker_id }
    return MeasurementHistoryItem(
        id = id,
        siteName = site_name,
        cellName = cell_name,
        measuredAt = measured_at,
        observationCount = observations.size,
        distances = relations.map { relation ->
            MarkerDistanceItem(
                id = relation.id,
                fromLabel = markers[relation.from_marker_id].markerLabel(),
                toLabel = markers[relation.to_marker_id].markerLabel(),
                distanceMm = relation.distance_mm.takeIf { it.isFinite() && it >= 0.0 }
            )
        }
    )
}

private fun ObservationResponse?.markerLabel(): String = if (this == null) {
    "Unknown marker"
} else {
    "ID $aruco_id - $dictionary"
}
