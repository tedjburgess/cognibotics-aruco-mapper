package com.example.cogniboticsarucomapper.data

import com.example.cogniboticsarucomapper.domain.MarkerDetection
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Dictionary
import org.opencv.objdetect.Objdetect
import kotlin.math.hypot

private const val CORNER_COUNT = 4

class MarkerDetector(
    val dictionaryCode: Int,
    detectorParameters: DetectorParameters,
) {
    private val dictionary: Dictionary = Objdetect.getPredefinedDictionary(dictionaryCode)
    private val detector = ArucoDetector(dictionary, detectorParameters)

    fun detect(
        gray: Mat,
        poseSolver: PoseSolver,
        cameraMatrix: Mat,
        distortionCoefficients: MatOfDouble,
    ): List<MarkerDetection> {
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()

        try {
            detector.detectMarkers(gray, corners, ids)
            if (corners.isEmpty() || ids.empty()) {
                return emptyList()
            }

            val detections = ArrayList<MarkerDetection>()
            for (index in corners.indices) {
                val markerCorners = corners[index]
                val pose = poseSolver.solve(markerCorners, cameraMatrix, distortionCoefficients)  ?: continue

                detections.add(
                    MarkerDetection(
                        dictionary = dictionaryCode,
                        markerId = readMarkerId(ids, index, corners.size),
                        pose = pose,
                        edgeLengthPx = averageEdgeLengthPx(markerCorners),
                    ),
                )
            }
            return detections
        } finally {
            ids.release()
            for (markerCorners in corners) {
                markerCorners.release()
            }
        }
    }

    private fun readMarkerId(ids: MatOfInt, index: Int, count: Int): Int {
        if (ids.cols() == count) {
            return ids.get(0, index)[0].toInt()
        }

        return ids.get(index, 0)[0].toInt()
    }

    private fun averageEdgeLengthPx(corners: Mat): Double {
        var totalLength = 0.0
        
        for (index in 0 until CORNER_COUNT) {
            val from = corners.get(0, index)
            val to = corners.get(0, (index + 1) % CORNER_COUNT)
            totalLength += hypot(from[0] - to[0], from[1] - to[1])
        }

        return totalLength / CORNER_COUNT
    }
}
