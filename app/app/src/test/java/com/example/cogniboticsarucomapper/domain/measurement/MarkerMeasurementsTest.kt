package com.example.cogniboticsarucomapper.domain.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerMeasurementsTest {
    @Test
    fun usesAgreedPhysicalSizes() {
        assertEquals(listOf(20.0, 50.0, 75.0, 100.0), MarkerFamily.entries.map { it.sizeMm })
    }

    @Test
    fun distanceIncludesDepthInsteadOfProjectingOntoOnePlane() {
        val results = MarkerMeasurements.distances(
            listOf(
                marker(MarkerFamily.FOUR_BY_FOUR, 0, 0.0, 0.0, 500.0),
                marker(MarkerFamily.SEVEN_BY_SEVEN, 1, 300.0, 0.0, 900.0)
            )
        )
        assertEquals(500.0, results.single().distanceMm, 1e-9)
    }

    @Test
    fun sameIdInDifferentFamiliesRemainsDistinct() {
        val results = MarkerMeasurements.distances(
            listOf(
                marker(MarkerFamily.FOUR_BY_FOUR, 0, 0.0, 0.0, 500.0),
                marker(MarkerFamily.FIVE_BY_FIVE, 0, 0.0, 300.0, 500.0)
            )
        )
        assertEquals(300.0, results.single().distanceMm, 1e-9)
    }

    @Test
    fun duplicateIdentitiesAreExcludedInsteadOfChoosingAnArbitraryMarker() {
        val a = marker(MarkerFamily.FOUR_BY_FOUR, 0, 0.0, 0.0, 500.0)
        val b = marker(MarkerFamily.FIVE_BY_FIVE, 0, 100.0, 0.0, 500.0)
        assertTrue(
            MarkerMeasurements.distances(
                listOf(a, a.copy(position = PositionMm(300.0, 0.0, 500.0)), b)
            ).isEmpty()
        )
    }

    @Test
    fun missingAndInvalidPosesCannotProduceDistances() {
        val valid = marker(MarkerFamily.FOUR_BY_FOUR, 0, 0.0, 0.0, 500.0)
        for (position in listOf(
            PositionMm(Double.NaN, 0.0, 500.0),
            PositionMm(0.0, 0.0, -500.0),
            PositionMm(0.0, 0.0, 0.0)
        )) {
            val invalid = PositionedMarker(MarkerKey(MarkerFamily.FIVE_BY_FIVE, 1), position)
            assertTrue(MarkerMeasurements.distances(listOf(valid, invalid)).isEmpty())
        }
        assertTrue(MarkerMeasurements.distances(emptyList()).isEmpty())
        assertTrue(MarkerMeasurements.distances(listOf(valid)).isEmpty())
    }

    private fun marker(family: MarkerFamily, id: Int, x: Double, y: Double, z: Double) =
        PositionedMarker(MarkerKey(family, id), PositionMm(x, y, z))
}
