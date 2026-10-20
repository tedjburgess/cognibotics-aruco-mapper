package com.example.cogniboticsarucomapper.vision

import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerCertaintyTrackerTest {
    @Test
    fun tracksFamiliesSeparatelyAndResetsWhenAMarkerDisappears() {
        val tracker = MarkerCertaintyTracker(10)
        val a = MarkerKey(ArucoDictionary.DICT_4X4_50, 0)
        val b = MarkerKey(ArucoDictionary.DICT_5X5_50, 0)
        repeat(10) { tracker.update(setOf(a)) }
        val frame = tracker.update(setOf(a, b))
        assertEquals(MarkerScanState.STABLE, frame.getValue(a).state)
        assertEquals(1, frame.getValue(b).consecutiveFrames)
        assertTrue(tracker.update(emptySet()).isEmpty())
        assertEquals(1, tracker.update(setOf(a)).getValue(a).consecutiveFrames)
    }
}
