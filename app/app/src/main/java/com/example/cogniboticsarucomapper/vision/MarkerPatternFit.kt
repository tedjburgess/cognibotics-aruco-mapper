package com.example.cogniboticsarucomapper.vision

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.Objdetect

internal object MarkerPatternFit {
    private const val SIDE = 144

    fun error(gray: Mat, marker: VisionEvent.MarkerDetected): Double {
        val source =
            MatOfPoint2f(
                *marker.corners.map {
                    Point(it.x.toDouble(), it.y.toDouble())
                }.toTypedArray()
            )
        val last = (SIDE - 1).toDouble()
        val target =
            MatOfPoint2f(Point(0.0, 0.0), Point(last, 0.0), Point(last, last), Point(0.0, last))
        val warped = Mat()
        val binary = Mat()
        val expected = Mat()
        val difference = Mat()
        var transform: Mat? = null
        return try {
            transform = Imgproc.getPerspectiveTransform(source, target)
            Imgproc.warpPerspective(gray, warped, transform, Size(SIDE.toDouble(), SIDE.toDouble()))
            Imgproc.threshold(
                warped,
                binary,
                0.0,
                255.0,
                Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
            )
            Objdetect.generateImageMarker(
                Objdetect.getPredefinedDictionary(marker.dictionary.toOpenCvDictionaryId()),
                marker.markerId,
                SIDE,
                expected
            )
            Core.compare(binary, expected, difference, Core.CMP_NE)
            Core.countNonZero(difference).toDouble() / (SIDE * SIDE)
        } catch (_: Exception) {
            Double.POSITIVE_INFINITY
        } finally {
            source.release()
            target.release()
            transform?.release()
            warped.release()
            binary.release()
            expected.release()
            difference.release()
        }
    }
}
