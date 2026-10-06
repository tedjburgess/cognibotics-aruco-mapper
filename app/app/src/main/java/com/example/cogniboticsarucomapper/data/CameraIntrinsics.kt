package com.example.cogniboticsarucomapper.data

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble

data class SensorGeometry(
    val focalLengthMm: Double,
    val sensorWidthMm: Double,
    val sensorHeightMm: Double,
)

data class CameraIntrinsics(
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
) {
    fun cameraMatrix(): Mat {
        val matrix = Mat(3, 3, CvType.CV_64F)

        matrix.put(0, 0, fx)
        matrix.put(0, 1, 0.0)
        matrix.put(0, 2, cx)
        matrix.put(1, 0, 0.0)
        matrix.put(1, 1, fy)
        matrix.put(1, 2, cy)
        matrix.put(2, 0, 0.0)
        matrix.put(2, 1, 0.0)
        matrix.put(2, 2, 1.0)

        return matrix
    }

    fun distortionCoefficients(): MatOfDouble {
        return MatOfDouble()
    }

    companion object {
        fun fromSensor(
            geometry: SensorGeometry?,
            imageWidth: Int,
            imageHeight: Int,
        ): CameraIntrinsics? {
            if (geometry == null) {
                return null
            }
            if (imageWidth <= 0 || imageHeight <= 0) {
                return null
            }
            if (geometry.focalLengthMm <= 0.0) {
                return null
            }
            if (geometry.sensorWidthMm <= 0.0 || geometry.sensorHeightMm <= 0.0) {
                return null
            }

            val imageAspect = imageWidth.toDouble() / imageHeight
            val sensorAspect = geometry.sensorWidthMm / geometry.sensorHeightMm

            var visibleWidthMm = geometry.sensorWidthMm
            if (imageAspect < sensorAspect) {
                visibleWidthMm = geometry.sensorHeightMm * imageAspect
            }

            val pixelsPerMm = imageWidth / visibleWidthMm

            return CameraIntrinsics(
                fx = geometry.focalLengthMm * pixelsPerMm,
                fy = geometry.focalLengthMm * pixelsPerMm,
                cx = imageWidth / 2.0,
                cy = imageHeight / 2.0,
            )
        }
    }
}
