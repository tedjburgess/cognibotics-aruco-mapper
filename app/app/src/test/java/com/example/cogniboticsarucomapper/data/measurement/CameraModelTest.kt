package com.example.cogniboticsarucomapper.data.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraModelTest {
    @Test
    fun metadataEstimateIsExplicitlyUncalibratedAndUsesAnalysisAspectRatio() {
        val model = SensorGeometry(4.0, 6.4, 4.8).estimate(640, 480)!!
        assertEquals(400.0, model.fx, 1e-9)
        assertEquals(400.0, model.fy, 1e-9)
        assertFalse(model.calibrated)
        val cropped = SensorGeometry(4.0, 6.4, 4.8).estimate(480, 480)!!
        assertEquals(400.0, cropped.fx, 1e-9)
    }

    @Test
    fun calibrationCannotBeReusedAtADifferentResolution() {
        val model = CameraModel(640, 480, 800.0, 800.0, 320.0, 240.0, calibrated = true)
        assertTrue(model.matches(640, 480))
        assertFalse(model.matches(1280, 960))
        assertFalse(model.copy(fx = Double.NaN).isValid)
        assertFalse(model.copy(distortion = listOf(1.0)).isValid)
    }

    @Test
    fun invalidMetadataDoesNotInventCameraParameters() {
        assertNull(SensorGeometry(0.0, 6.4, 4.8).estimate(640, 480))
        assertNull(SensorGeometry(4.0, Double.NaN, 4.8).estimate(640, 480))
        assertNull(SensorGeometry(4.0, 6.4, 4.8).estimate(0, 480))
    }
}
