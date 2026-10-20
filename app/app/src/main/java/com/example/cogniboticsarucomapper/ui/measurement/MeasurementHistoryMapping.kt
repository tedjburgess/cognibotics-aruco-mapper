package com.example.cogniboticsarucomapper.ui.measurement

import com.example.cogniboticsarucomapper.network.model.ObservationResponse
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.util.MeasurementFormats

data class MarkerDistanceItem(
    val id: String,
    val fromLabel: String,
    val toLabel: String,
    val distanceMm: Double?
)

/** Preserve the backend's distances; do not infer a new distance from screen coordinates. */
fun ScanResponse.toMeasurementHistoryItem(): MeasurementHistoryItem {
    val markers = observations.associateBy { it.markerId }
    return MeasurementHistoryItem(
        id = id,
        siteName = siteName,
        cellName = cellName,
        measuredAt = measuredAt,
        observationCount = observations.size,
        distances = relations.map { relation ->
            MarkerDistanceItem(
                id = relation.id,
                fromLabel = markers[relation.fromMarkerId].markerLabel(),
                toLabel = markers[relation.toMarkerId].markerLabel(),
                distanceMm = relation.distanceMm.takeIf { it.isFinite() && it >= 0.0 }
            )
        }
    )
}

private fun ObservationResponse?.markerLabel(): String = if (this == null) {
    "Unknown marker"
} else {
    MeasurementFormats.historyMarkerLabel(arucoId, dictionary)
}
