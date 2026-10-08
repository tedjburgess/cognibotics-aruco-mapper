package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import org.opencv.core.Mat
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect

class ArucoAnalyzer : VisionEngine.FrameAnalyzer {

    private val dictionary =
        Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50)

    private val detector = ArucoDetector(dictionary)

    override fun processFrame(
        gray: Mat,
        color: Mat,
        timestampMs: Long,
        rotationDegrees: Int
    ): List<VisionEvent> {

        val corners = mutableListOf<Mat>()
        val ids = Mat()

        try {
            detector.detectMarkers(gray, corners, ids)

            if (ids.empty()) {
                return listOf(
                    VisionEvent.MarkersCleared(
                        timestampMs = timestampMs
                    )
                )
            }

            val events = mutableListOf<VisionEvent>()

            for (i in 0 until ids.rows()) {

                val markerId =
                    ids.get(i, 0)[0].toInt()

                val markerCorners =
                    corners[i]

                val offsets = (0 until 4).map { cornerIndex ->

                    val point =
                        markerCorners.get(0, cornerIndex)

                    Offset(
                        x = point[0].toFloat(),
                        y = point[1].toFloat()
                    )
                }

                val center = Offset(
                    x = offsets.map { it.x }.average().toFloat(),
                    y = offsets.map { it.y }.average().toFloat()
                )

                events.add(
                    VisionEvent.MarkerDetected(
                        markerId = markerId,
                        corners = offsets,
                        center = center,
                        timestampMs = timestampMs,
                        frameWidth = gray.cols(),
                        frameHeight = gray.rows(),
                        rotationDegrees = rotationDegrees
                    )
                )
            }

            return events

        } finally {

            ids.release()

            corners.forEach { corner ->
                corner.release()
            }
        }
    }
}