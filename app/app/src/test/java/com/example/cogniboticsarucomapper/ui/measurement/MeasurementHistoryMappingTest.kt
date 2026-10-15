package com.example.cogniboticsarucomapper.ui.measurement

import com.example.cogniboticsarucomapper.network.model.ObservationResponse
import com.example.cogniboticsarucomapper.network.model.OrientationDto
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.network.model.RelationResponse
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementHistoryMappingTest {
    private val markerA = marker("a", "DICT_4X4_50")
    private val markerB = marker("b", "DICT_5X5_50")

    @Test
    fun preservesBackendDistanceAndDistinguishesSameIdInDifferentDictionaries() {
        val distance = scan(relation(312.456789)).toMeasurementHistoryItem().distances.single()
        assertEquals(312.456789, distance.distanceMm!!, 0.0)
        assertEquals("Marker 0 (DICT_4X4_50)", distance.fromLabel)
        assertEquals("Marker 0 (DICT_5X5_50)", distance.toLabel)
    }

    @Test
    fun missingEndpointDoesNotCrashOrMislabelTheOtherMarker() {
        val distance = scan(relation(300.0).copy(to_marker_id = "missing"))
            .toMeasurementHistoryItem().distances.single()
        assertEquals("Unknown marker", distance.toLabel)
        assertEquals("Marker 0 (DICT_4X4_50)", distance.fromLabel)
    }

    @Test
    fun invalidValuesAreUnavailableInsteadOfDisplayedAsMeasurements() {
        for (value in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertNull(
                scan(relation(value)).toMeasurementHistoryItem().distances.single().distanceMm
            )
        }
    }

    @Test
    fun scanWithoutRelationsDoesNotInventDistancesFromPositions() {
        val item = scan().toMeasurementHistoryItem()
        assertEquals(2, item.observationCount)
        assertEquals(emptyList<MarkerDistanceItem>(), item.distances)
        assertEquals("Lab", item.siteName)
    }

    private fun marker(id: String, dictionary: String) = ObservationResponse(
        id = "observation-$id",
        marker_id = id,
        aruco_id = 0,
        dictionary = dictionary,
        name = null,
        position = PositionDto(0.0, 0.0, 0.0),
        orientation = OrientationDto(0.0, 0.0, 0.0, 1.0),
        size_mm = 20.0
    )

    private fun relation(value: Double) = RelationResponse(
        id = "relation",
        from_marker_id = "a",
        to_marker_id = "b",
        distance_mm = value,
        relative_position = null
    )

    private fun scan(vararg relations: RelationResponse) = ScanResponse(
        id = "scan",
        site_name = "Lab",
        cell_name = "Cell",
        device_id = "Phone",
        measured_at = "2026-10-13T12:00:00Z",
        created_at = "2026-10-13T12:00:00Z",
        observations = listOf(markerA, markerB),
        relations = relations.toList()
    )
}
