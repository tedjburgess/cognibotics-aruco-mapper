package com.example.cogniboticsarucomapper.domain.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceDisplayTrackerTest {
    private val a = MarkerKey(ArucoDictionary.DICT_4X4_50, 0)
    private val b = MarkerKey(ArucoDictionary.DICT_4X4_50, 1)
    private val c = MarkerKey(ArucoDictionary.DICT_5X5_50, 0)

    @Test fun missingReadingKeepsItsRowButNeverItsValue() {
        val tracker = DistanceDisplayTracker()
        tracker.update(setOf(c, b, a), listOf(MarkerPairDistance(a, b, 135.0)), 0)
        val missing = tracker.update(setOf(a, c), emptyList(), 30)
        assertEquals(3, missing.size)
        assertEquals(a, missing.first().from)
        assertEquals(b, missing.first().to)
        assertTrue(missing.all { it.rawMm == null && it.steadyMm == null })
        tracker.reset()
        assertTrue(tracker.update(emptySet(), emptyList(), 60).isEmpty())
    }

    @Test fun medianResistsAnOutlierAndRawErrorStaysHonest() {
        val tracker = DistanceDisplayTracker()
        for ((index, value) in listOf(134.0, 135.0, 136.0, 135.0, 220.0).withIndex()) {
            val row = tracker.update(
                setOf(a, b),
                listOf(MarkerPairDistance(a, b, value)),
                index * 30L
            ).single()
            assertEquals(value, row.rawMm!!, 0.0)
        }
        val row = tracker.update(setOf(a, b), listOf(MarkerPairDistance(a, b, 140.0)), 150).single()
        assertEquals(136.0, row.steadyMm!!, 0.0)
        assertEquals(5.0, row.absoluteTestErrorMm!!, 0.0)
        assertTrue(row.isTestSheetPair)
        val expired = tracker.update(
            setOf(a, b),
            listOf(MarkerPairDistance(a, b, 150.0)),
            1000
        ).single()
        assertEquals(150.0, expired.steadyMm!!, 0.0)
    }
}
