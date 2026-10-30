package com.example.cogniboticsarucomapper.domain.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerMeasurementsTest {
    @Test
    fun usesAgreedPhysicalSizes() {
        assertEquals(
            listOf(20.0, 50.0, 75.0, 100.0, 110.0),
            ArucoDictionary.entries.map {
                it.sizeMm
            }
        )
    }

    @Test
    fun distanceIncludesDepthInsteadOfProjectingOntoOnePlane() {
        val results = MarkerMeasurements.distances(
            listOf(
                marker(ArucoDictionary.DICT_4X4_50, 0, 0.0, 0.0, 500.0),
                marker(ArucoDictionary.DICT_7X7_50, 1, 300.0, 0.0, 900.0)
            )
        )
        assertEquals(500.0, results.single().distanceMm, 1e-9)
    }

    @Test
    fun sameIdInDifferentFamiliesRemainsDistinct() {
        val results = MarkerMeasurements.distances(
            listOf(
                marker(ArucoDictionary.DICT_4X4_50, 0, 0.0, 0.0, 500.0),
                marker(ArucoDictionary.DICT_5X5_50, 0, 0.0, 300.0, 500.0)
            )
        )
        assertEquals(300.0, results.single().distanceMm, 1e-9)
    }

    @Test
    fun duplicateIdentitiesAreExcludedInsteadOfChoosingAnArbitraryMarker() {
        val a = marker(ArucoDictionary.DICT_4X4_50, 0, 0.0, 0.0, 500.0)
        val b = marker(ArucoDictionary.DICT_5X5_50, 0, 100.0, 0.0, 500.0)
        assertTrue(
            MarkerMeasurements.distances(
                listOf(a, a.copy(position = PositionMm(300.0, 0.0, 500.0)), b)
            ).isEmpty()
        )
    }

    @Test
    fun missingAndInvalidPosesCannotProduceDistances() {
        val valid = marker(ArucoDictionary.DICT_4X4_50, 0, 0.0, 0.0, 500.0)
        for (position in listOf(
            PositionMm(Double.NaN, 0.0, 500.0),
            PositionMm(0.0, 0.0, -500.0),
            PositionMm(0.0, 0.0, 0.0)
        )) {
            val invalid = PositionedMarker(MarkerKey(ArucoDictionary.DICT_5X5_50, 1), position)
            assertTrue(MarkerMeasurements.distances(listOf(valid, invalid)).isEmpty())
        }
        assertTrue(MarkerMeasurements.distances(emptyList()).isEmpty())
        assertTrue(MarkerMeasurements.distances(listOf(valid)).isEmpty())
    }

    private fun marker(dictionary: ArucoDictionary, id: Int, x: Double, y: Double, z: Double) =
        PositionedMarker(MarkerKey(dictionary, id), PositionMm(x, y, z))
}
