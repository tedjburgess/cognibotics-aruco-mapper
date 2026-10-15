package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.data.measurement.CameraModel
import com.example.cogniboticsarucomapper.data.measurement.MarkerPoseSolver
import com.example.cogniboticsarucomapper.data.measurement.SensorGeometry
import com.example.cogniboticsarucomapper.domain.measurement.ImagePoint
import com.example.cogniboticsarucomapper.domain.measurement.MarkerFamily
import com.example.cogniboticsarucomapper.domain.measurement.MarkerImageQuality
import com.example.cogniboticsarucomapper.domain.measurement.MarkerMeasurements
import com.example.cogniboticsarucomapper.domain.measurement.PositionedMarker
import org.opencv.core.Mat
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect

class ArucoAnalyzer : VisionEngine.FrameAnalyzer {
    @Volatile var sensorGeometry: SensorGeometry? = null

    // Calibration capture/persistence can supply a profile here without changing the solver.
    // A supplied profile must match the actual analysis resolution; never silently reuse a mismatch.
    @Volatile var calibratedCameraModel: CameraModel? = null

    private val solver = MarkerPoseSolver()

    // Test mode only accepts the IDs printed in our accuracy pack; normal mode accepts all IDs.
    @Volatile var testSheetMode: Boolean = true
    private val detectors = listOf(
        MarkerFamily.FOUR_BY_FOUR to Objdetect.DICT_4X4_1000,
        MarkerFamily.FIVE_BY_FIVE to Objdetect.DICT_5X5_1000,
        MarkerFamily.SIX_BY_SIX to Objdetect.DICT_6X6_1000,
        MarkerFamily.SEVEN_BY_SEVEN to Objdetect.DICT_7X7_1000
    ).map { (family, code) ->
        val parameters = DetectorParameters().apply {
            _cornerRefinementMethod = Objdetect.CORNER_REFINE_SUBPIX
            // Small 4x4 codes must match exactly rather than repair background patterns.
            _errorCorrectionRate = if (family == MarkerFamily.FOUR_BY_FOUR) 0.0 else 0.4
        }
        family to ArucoDetector(Objdetect.getPredefinedDictionary(code), parameters)
    }

    override fun processFrame(
        gray: Mat,
        color: Mat,
        timestampMs: Long,
        rotationDegrees: Int
    ): List<VisionEvent> {
        val testing = testSheetMode
        val calibrated = calibratedCameraModel
        val model = if (calibrated != null) {
            calibrated.takeIf { it.calibrated && it.matches(gray.cols(), gray.rows()) }
        } else {
            sensorGeometry?.estimate(gray.cols(), gray.rows())
        }
        val markers = mutableListOf<VisionEvent.MarkerDetected>()
        for ((family, detector) in detectors) {
            val corners = mutableListOf<Mat>()
            val ids = Mat()
            try {
                detector.detectMarkers(gray, corners, ids)
                for (index in corners.indices) {
                    val offsets = (0 until 4).map { corner ->
                        val point = corners[index].get(0, corner)
                        Offset(point[0].toFloat(), point[1].toFloat())
                    }
                    val id = ids.get(index, 0)[0].toInt()
                    if (testing && id !in 0..3) continue
                    val gridCells = family.ordinal + 6
                    // Reject tiny or severely compressed candidates before estimating their position.
                    if (!MarkerImageQuality.hasEnoughDetail(
                            offsets.map {
                                ImagePoint(it.x.toDouble(), it.y.toDouble())
                            },
                            gridCells
                        )
                    ) {
                        continue
                    }
                    val pose = model?.let { solver.estimate(offsets, family.sizeMm, it, gridCells) }
                    markers.add(
                        VisionEvent.MarkerDetected(
                            markerId = id,
                            corners = offsets,
                            center = Offset(
                                offsets.map { it.x }.average().toFloat(),
                                offsets.map { it.y }.average().toFloat()
                            ),
                            timestampMs = timestampMs,
                            frameWidth = gray.cols(),
                            frameHeight = gray.rows(),
                            rotationDegrees = rotationDegrees,
                            family = family,
                            positionMm = pose?.position,
                            tiltDegrees = pose?.tiltDegrees
                        )
                    )
                }
            } finally {
                ids.release()
                corners.forEach { it.release() }
            }
        }
        val uniqueKeys = markers.groupingBy { it.key }.eachCount().filterValues { it == 1 }.keys
        val positioned = markers.filter { it.key in uniqueKeys }.mapNotNull { marker ->
            marker.positionMm?.let { PositionedMarker(marker.key, it) }
        }
        return listOf(
            VisionEvent.FrameMeasured(
                markers = markers,
                distances = MarkerMeasurements.distances(positioned),
                testSheetMode = testing,
                cameraParameters = when {
                    model == null -> CameraParameterSource.UNAVAILABLE
                    model.calibrated -> CameraParameterSource.CALIBRATED
                    else -> CameraParameterSource.ESTIMATED
                }
            )
        )
    }
}
