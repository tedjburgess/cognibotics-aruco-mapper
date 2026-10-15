package com.example.cogniboticsarucomapper.domain.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerImageQualityTest {
    private fun rectangle(width: Double, height: Double) = listOf(
        ImagePoint(0.0, 0.0),
        ImagePoint(width, 0.0),
        ImagePoint(width, height),
        ImagePoint(0.0, height)
    )

    @Test fun rejectsTinyOrSeverelyCompressedMarkers() {
        assertTrue(MarkerImageQuality.hasEnoughDetail(rectangle(32.0, 32.0), 6))
        assertFalse(MarkerImageQuality.hasEnoughDetail(rectangle(6.0, 6.0), 6))
        assertFalse(MarkerImageQuality.hasEnoughDetail(rectangle(100.0, 5.0), 9))
        assertEquals(5.0, MarkerImageQuality.thicknessPixels(rectangle(100.0, 5.0)), 0.0)
    }

    @Test fun rejectsCrossedAndInvalidCorners() {
        val points = rectangle(32.0, 32.0)
        assertFalse(
            MarkerImageQuality.hasEnoughDetail(
                listOf(points[0], points[2], points[1], points[3]),
                6
            )
        )
        assertFalse(MarkerImageQuality.hasEnoughDetail(points.drop(1), 6))
        assertFalse(MarkerImageQuality.hasEnoughDetail(points + ImagePoint(Double.NaN, 0.0), 6))
    }
}
