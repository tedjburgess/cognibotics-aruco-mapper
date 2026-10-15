package com.example.cogniboticsarucomapper.data.measurement

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo
import kotlin.math.min

/** A profile must describe the analysis image, not the scaled/cropped on-screen preview. */
data class CameraModel(
    val width: Int,
    val height: Int,
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
    val distortion: List<Double> = emptyList(),
    val calibrated: Boolean = false
) {
    val isValid: Boolean get() = width > 0 &&
        height > 0 &&
        fx.isFinite() &&
        fy.isFinite() &&
        fx > 0.0 &&
        fy > 0.0 &&
        cx.isFinite() &&
        cy.isFinite() &&
        distortion.size in listOf(0, 4, 5, 8, 12, 14) &&
        distortion.all { it.isFinite() }

    fun matches(frameWidth: Int, frameHeight: Int): Boolean =
        isValid && width == frameWidth && height == frameHeight
}

/** Provisional pinhole approximation. This is not a checkerboard calibration. */
data class SensorGeometry(val focalMm: Double, val widthMm: Double, val heightMm: Double) {
    fun estimate(width: Int, height: Int): CameraModel? {
        if (width <= 0 ||
            height <= 0 ||
            listOf(focalMm, widthMm, heightMm).any { !it.isFinite() || it <= 0.0 }
        ) {
            return null
        }
        val visibleWidth = min(widthMm, heightMm * width.toDouble() / height)
        val focalPixels = focalMm * width / visibleWidth
        return CameraModel(width, height, focalPixels, focalPixels, width / 2.0, height / 2.0)
            .takeIf { it.isValid }
    }
}

@androidx.annotation.OptIn(markerClass = [ExperimentalCamera2Interop::class])
fun sensorGeometryFor(cameraInfo: CameraInfo): SensorGeometry? = runCatching {
    // Read the camera CameraX actually bound, rather than the first rear camera in a list.
    val camera = Camera2CameraInfo.from(cameraInfo)
    val focal =
        camera.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            ?.singleOrNull() ?: return null
    val size = camera.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        ?: return null
    SensorGeometry(focal.toDouble(), size.width.toDouble(), size.height.toDouble())
}.getOrNull()
