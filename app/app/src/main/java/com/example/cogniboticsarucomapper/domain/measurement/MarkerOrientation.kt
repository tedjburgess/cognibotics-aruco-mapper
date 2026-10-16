package com.example.cogniboticsarucomapper.domain.measurement

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Unit quaternion representing the marker-to-camera rotation from OpenCV solvePnP.
 * This is a domain measurement, not an API DTO.
 */
data class MarkerOrientation(val x: Double, val y: Double, val z: Double, val w: Double) {
    val isValid: Boolean
        get() = listOf(x, y, z, w).all { it.isFinite() } &&
            abs(x * x + y * y + z * z + w * w - 1.0) < 1e-6

    companion object {
        /** Rodrigues rotation vector (axis times angle in radians) to a unit quaternion. */
        fun fromRotationVector(x: Double, y: Double, z: Double): MarkerOrientation? {
            if (!x.isFinite() || !y.isFinite() || !z.isFinite()) return null

            val angle = sqrt(x * x + y * y + z * z)
            if (!angle.isFinite()) return null

            // sin(angle / 2) / angle approaches 1/2 at zero, avoiding division by zero.
            val scale = if (angle < 1e-8) 0.5 else sin(angle / 2.0) / angle
            return MarkerOrientation(
                x = x * scale,
                y = y * scale,
                z = z * scale,
                w = cos(angle / 2.0)
            ).takeIf { it.isValid }
        }
    }
}
