package com.example.cogniboticsarucomapper.vision
import androidx.compose.ui.geometry.Offset
import org.opencv.objdetect.Dictionary
import org.opencv.objdetect.Objdetect

import org.opencv.core.Mat
import org.opencv.objdetect.ArucoDetector

class ArucoAnalyzer : VisionEngine.FrameAnalyzer {
    private val dictionary =
        Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50)
    private val detector = ArucoDetector(dictionary)


    override fun processFrame(
        gray: Mat,
        color: Mat,
        timestampMs: Long
    ): List<VisionEvent> {
        val corners = mutableListOf<Mat>()
        val ids = Mat()

        detector.detectMarkers(gray, corners, ids)

        if (ids.empty()) {
            return emptyList()
        }

        val markerId = ids.get(0, 0)[0].toInt()
        val markerCorners = corners[0]

        val firstCorner  = markerCorners.get(0, 0)
        val secondCorner = markerCorners.get(0, 1)
        val thirdCorner  = markerCorners.get(0, 2)
        val fourthCorner = markerCorners.get(0, 3)

        //Converting the open cv markers to List<Offset>
        val firstOffset = Offset(
            firstCorner[0].toFloat(),
            firstCorner[1].toFloat()
        )

        val secondOffset = Offset(
            secondCorner[0].toFloat(),
            secondCorner[0].toFloat()
        )

        val thirdOffset = Offset(
            thirdCorner[0].toFloat(),
            thirdCorner[0].toFloat()
        )

        val fourthOffset = Offset(
            fourthCorner[0].toFloat(),
            fourthCorner[0].toFloat()
        )

        val markerOffsets = listOf(
            firstOffset,
            secondOffset,
            thirdOffset,
            fourthOffset
        )

        val center = Offset(
            (firstOffset.x + secondOffset.x + thirdOffset.x + fourthOffset.x) / 4f,
            (firstOffset.y + secondOffset.y + thirdOffset.y + fourthOffset.y) / 4f
        )

        val event = VisionEvent.MarkerDetected(
            markerId = markerId,
            corners = markerOffsets,
            center = center,
            timestampMs = timestampMs
        )

        return listOf(event)
    }
}