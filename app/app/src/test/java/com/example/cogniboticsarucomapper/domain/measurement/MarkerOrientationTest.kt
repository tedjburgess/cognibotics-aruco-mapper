package com.example.cogniboticsarucomapper.domain.measurement

import kotlin.math.PI
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerOrientationTest {
    @Test
    fun zeroRotationProducesIdentityQuaternion() {
        val orientation = MarkerOrientation.fromRotationVector(0.0, 0.0, 0.0)
        assertNotNull(orientation)
        assertEquals(0.0, orientation!!.x, 1e-12)
        assertEquals(0.0, orientation.y, 1e-12)
        assertEquals(0.0, orientation.z, 1e-12)
        assertEquals(1.0, orientation.w, 1e-12)
        assertTrue(orientation.isValid)
    }

    @Test
    fun quarterTurnAboutZPreservesTrueRotation() {
        val orientation = MarkerOrientation.fromRotationVector(0.0, 0.0, PI / 2.0)!!
        assertEquals(0.0, orientation.x, 1e-12)
        assertEquals(0.0, orientation.y, 1e-12)
        assertEquals(sqrt(0.5), orientation.z, 1e-12)
        assertEquals(sqrt(0.5), orientation.w, 1e-12)
        assertTrue(orientation.isValid)
    }

    @Test
    fun halfTurnAboutXIsNotMistakenForIdentity() {
        val orientation = MarkerOrientation.fromRotationVector(PI, 0.0, 0.0)!!
        assertEquals(1.0, orientation.x, 1e-12)
        assertEquals(0.0, orientation.w, 1e-12)
        assertTrue(orientation.isValid)
    }

    @Test
    fun invalidRotationsDoNotProduceOrientation() {
        assertNull(MarkerOrientation.fromRotationVector(Double.NaN, 0.0, 0.0))
        assertNull(MarkerOrientation.fromRotationVector(0.0, Double.POSITIVE_INFINITY, 0.0))
        assertFalse(MarkerOrientation(0.0, 0.0, 0.0, 0.0).isValid)
    }
}
