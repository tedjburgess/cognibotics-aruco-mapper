package com.example.cogniboticsarucomapper.data.measurement

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.domain.measurement.MarkerFamily
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import com.example.cogniboticsarucomapper.domain.measurement.MarkerOrientation
import com.example.cogniboticsarucomapper.domain.measurement.MarkerPairDistance
import com.example.cogniboticsarucomapper.domain.measurement.PositionMm
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.vision.CameraParameterSource
import com.example.cogniboticsarucomapper.vision.VisionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementRequestMapperTest {
    private val identity = MarkerOrientation.fromRotationVector(0.0, 0.0, 0.0)!!

    @Test
    fun mapsAllMarkerFamiliesToExistingApiObservationPayloads() {
        val families = MarkerFamily.entries
        val markers = families.mapIndexed { index, family ->
            marker(family, index, PositionMm(0.0, 0.0, 500.0 + index))
        }
        val request = map(frame(markers))!!
        assertEquals(
            listOf("DICT_4X4_50", "DICT_5X5_50", "DICT_6X6_50", "DICT_7X7_50"),
            request.observations.map { it.dictionary }
        )
        assertEquals(listOf(20.0, 50.0, 75.0, 100.0), request.observations.map { it.size_mm })
        assertEquals(PositionDto(0.0, 0.0, 500.0), request.observations.first().position)
        assertEquals("lab", request.site_name)
        assertEquals("cell", request.cell_name)
        assertEquals("phone", request.device_id)
        assertEquals("2026-10-16T00:00:00Z", request.measured_at)
    }

    @Test
    fun mapsRealQuaternionRatherThanInventingIdentity() {
        val rotation = MarkerOrientation.fromRotationVector(0.0, 0.0, Math.PI / 2.0)!!
        val request = map(
            frame(
                listOf(marker(MarkerFamily.SIX_BY_SIX, 10, PositionMm(1.0, 2.0, 500.0), rotation))
            )
        )!!
        val orientation = request.observations.single().orientation
        assertEquals(rotation.z, orientation.z, 1e-12)
        assertEquals(rotation.w, orientation.w, 1e-12)
        assertTrue(orientation.z > 0.0)
    }

    @Test
    fun mapsMixedFamilyDistanceAndRelativePositionInMillimetres() {
        val first = marker(MarkerFamily.FOUR_BY_FOUR, 0, PositionMm(0.0, 0.0, 500.0))
        val second = marker(MarkerFamily.SEVEN_BY_SEVEN, 0, PositionMm(300.0, 400.0, 500.0))
        val pair = MarkerPairDistance(first.key, second.key, 500.0)
        val request = map(frame(listOf(second, first), listOf(pair)))!!
        val relation = request.relations.single()
        assertEquals(0, relation.from_aruco_id)
        assertEquals(0, relation.to_aruco_id)
        assertEquals("DICT_4X4_50", relation.from_dictionary)
        assertEquals("DICT_7X7_50", relation.to_dictionary)
        assertEquals(500.0, relation.distance_mm, 1e-12)
        assertEquals(PositionDto(300.0, 400.0, 0.0), relation.relative_position)
    }

    @Test
    fun neverMapsDuplicateUnpositionedOrUnorientedMarkersAndTheirRelations() {
        val original = marker(MarkerFamily.FOUR_BY_FOUR, 0, PositionMm(0.0, 0.0, 500.0))
        val duplicated = original.copy(positionMm = PositionMm(10.0, 0.0, 500.0))
        val missingRotation = marker(MarkerFamily.SIX_BY_SIX, 1, PositionMm(40.0, 0.0, 500.0))
            .copy(orientation = null)
        val valid = marker(MarkerFamily.SEVEN_BY_SEVEN, 2, PositionMm(0.0, 20.0, 500.0))
        val pair = MarkerPairDistance(original.key, valid.key, 20.0)
        val request = map(
            frame(listOf(original, duplicated, missingRotation, valid), listOf(pair))
        )!!
        assertEquals(listOf(2), request.observations.map { it.aruco_id })
        assertTrue(request.relations.isEmpty())
    }

    @Test
    fun dropsRelationIfDistanceContradictsMarkerPositions() {
        val first = marker(MarkerFamily.FOUR_BY_FOUR, 0, PositionMm(0.0, 0.0, 500.0))
        val second = marker(MarkerFamily.FIVE_BY_FIVE, 1, PositionMm(300.0, 400.0, 500.0))
        val request = map(
            frame(listOf(first, second), listOf(MarkerPairDistance(first.key, second.key, 100.0)))
        )!!
        assertEquals(2, request.observations.size)
        assertTrue(request.relations.isEmpty())
    }

    @Test
    fun refusesEmptyOrInvalidMetadataAndFramesWithoutValidPose() {
        assertNull(map(frame(emptyList())))
        assertNull(map(frame(listOf(marker(MarkerFamily.FOUR_BY_FOUR, 0, null)))))
        val valid = frame(
            listOf(marker(MarkerFamily.FIVE_BY_FIVE, 1, PositionMm(0.0, 0.0, 500.0)))
        )
        assertNull(MeasurementRequestMapper.fromFrame(valid, "", "cell", "phone", "time"))
        assertNotNull(map(valid))
    }

    private fun map(frame: VisionEvent.FrameMeasured) = MeasurementRequestMapper.fromFrame(
        frame = frame,
        siteName = "lab",
        cellName = "cell",
        deviceId = "phone",
        measuredAt = "2026-10-16T00:00:00Z"
    )

    private fun frame(
        markers: List<VisionEvent.MarkerDetected>,
        distances: List<MarkerPairDistance> = emptyList()
    ) = VisionEvent.FrameMeasured(
        markers = markers,
        distances = distances,
        cameraParameters = CameraParameterSource.CALIBRATED
    )

    private fun marker(
        family: MarkerFamily,
        id: Int,
        position: PositionMm?,
        orientation: MarkerOrientation? = identity
    ) = VisionEvent.MarkerDetected(
        markerId = id,
        corners = emptyList(),
        center = Offset.Zero,
        timestampMs = 0L,
        frameWidth = 640,
        frameHeight = 480,
        rotationDegrees = 0,
        family = family,
        positionMm = position,
        orientation = orientation
    )
}
