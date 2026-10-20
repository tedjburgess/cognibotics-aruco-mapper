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
        assertEquals("DICT_4X4_50 #0", distance.fromLabel)
        assertEquals("DICT_5X5_50 #0", distance.toLabel)
    }

    @Test
    fun missingEndpointDoesNotCrashOrMislabelTheOtherMarker() {
        val distance = scan(relation(300.0).copy(toMarkerId = "missing"))
            .toMeasurementHistoryItem().distances.single()
        assertEquals("Unknown marker", distance.toLabel)
        assertEquals("DICT_4X4_50 #0", distance.fromLabel)
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
        markerId = id,
        arucoId = 0,
        dictionary = dictionary,
        name = null,
        position = PositionDto(0.0, 0.0, 0.0),
        orientation = OrientationDto(0.0, 0.0, 0.0, 1.0),
        sizeMm = 20.0
    )

    private fun relation(value: Double) = RelationResponse(
        id = "relation",
        fromMarkerId = "a",
        toMarkerId = "b",
        distanceMm = value,
        relativePosition = null
    )

    private fun scan(vararg relations: RelationResponse) = ScanResponse(
        id = "scan",
        siteName = "Lab",
        cellName = "Cell",
        deviceId = "Phone",
        measuredAt = "2026-10-13T12:00:00Z",
        createdAt = "2026-10-13T12:00:00Z",
        observations = listOf(markerA, markerB),
        relations = relations.toList()
    )
}
