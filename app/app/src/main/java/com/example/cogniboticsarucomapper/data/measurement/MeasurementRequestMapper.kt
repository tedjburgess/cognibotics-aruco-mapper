package com.example.cogniboticsarucomapper.data.measurement

import com.example.cogniboticsarucomapper.network.model.ObservationRequest
import com.example.cogniboticsarucomapper.network.model.OrientationDto
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.network.model.RelationRequest
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.vision.ArucoDictionaryType
import com.example.cogniboticsarucomapper.vision.VisionEvent
import kotlin.math.abs

/**
 * Maps a single camera frame to the existing POST /api/v1/scans payload.
 * Saving/uploading is deliberately left to the calling UI workflow (#53).
 */
object MeasurementRequestMapper {
    fun fromFrame(
        frame: VisionEvent.FrameMeasured,
        siteName: String,
        cellName: String,
        deviceId: String,
        measuredAt: String
    ): ScanRequest? {
        if (listOf(siteName, cellName, deviceId, measuredAt).any { it.isBlank() }) return null

        // A marker identity must be unique within the frame. Do not arbitrarily pick duplicates.
        val validMarkers = frame.markers
            .groupBy { it.key }
            .values
            .mapNotNull { it.singleOrNull() }
            .filter {
                it.markerId >= 0 &&
                    it.positionMm?.isValid == true &&
                    it.orientation?.isValid == true
            }
            .sortedWith(compareBy({ it.family.ordinal }, { it.markerId }))

        if (validMarkers.isEmpty()) return null

        val markersByKey = validMarkers.associateBy { it.key }
        val observations = validMarkers.map { marker ->
            val position = requireNotNull(marker.positionMm)
            val orientation = requireNotNull(marker.orientation)
            ObservationRequest(
                aruco_id = marker.markerId,
                dictionary = dictionaryName(marker),
                position = PositionDto(position.x, position.y, position.z),
                orientation = OrientationDto(
                    orientation.x,
                    orientation.y,
                    orientation.z,
                    orientation.w
                ),
                size_mm = marker.family.sizeMm
            )
        }

        val relations = frame.distances.mapNotNull { pair ->
            val from = markersByKey[pair.from] ?: return@mapNotNull null
            val to = markersByKey[pair.to] ?: return@mapNotNull null
            if (pair.from == pair.to || !pair.distanceMm.isFinite() || pair.distanceMm < 0.0) {
                return@mapNotNull null
            }

            val start = requireNotNull(from.positionMm)
            val end = requireNotNull(to.positionMm)
            // All points are already in one camera coordinate frame and expressed in mm.
            val dx = end.x - start.x
            val dy = end.y - start.y
            val dz = end.z - start.z
            val calculatedDistance = start.distanceTo(end)
            // Never persist a stale/mismatched relation from a different frame.
            if (abs(calculatedDistance - pair.distanceMm) >
                maxOf(1e-6, calculatedDistance * 1e-6)
            ) {
                return@mapNotNull null
            }

            RelationRequest(
                from_aruco_id = from.markerId,
                from_dictionary = dictionaryName(from),
                to_aruco_id = to.markerId,
                to_dictionary = dictionaryName(to),
                distance_mm = pair.distanceMm,
                relative_position = PositionDto(dx, dy, dz)
            )
        }

        return ScanRequest(
            site_name = siteName.trim(),
            cell_name = cellName.trim(),
            device_id = deviceId.trim(),
            measured_at = measuredAt.trim(),
            observations = observations,
            relations = relations
        )
    }

    private fun dictionaryName(marker: VisionEvent.MarkerDetected): String =
        ArucoDictionaryType.entries.single { it.family == marker.family }.name
}
