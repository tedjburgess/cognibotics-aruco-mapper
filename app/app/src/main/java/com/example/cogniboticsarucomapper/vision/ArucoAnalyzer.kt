package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.data.measurement.CameraModel
import com.example.cogniboticsarucomapper.data.measurement.MarkerPoseSolver
import com.example.cogniboticsarucomapper.data.measurement.SensorGeometry
import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.ImagePoint
import com.example.cogniboticsarucomapper.domain.measurement.MarkerImageQuality
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import com.example.cogniboticsarucomapper.domain.measurement.MarkerMeasurements
import com.example.cogniboticsarucomapper.domain.measurement.PositionedMarker
import com.example.cogniboticsarucomapper.domain.measurement.role
import org.opencv.core.Mat
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect

// Default to mixed-family measurement; callers can still select a single family explicitly.
class ArucoAnalyzer(dictionary: ArucoDictionary? = null) : VisionEngine.FrameAnalyzer {
    @Volatile var sensorGeometry: SensorGeometry? = null

    // Calibration capture/persistence can supply a profile here without changing the solver.
    // A supplied profile must match the actual analysis resolution; never silently reuse a mismatch.
    @Volatile var calibratedCameraModel: CameraModel? = null

    private val solver = MarkerPoseSolver()

    private val dictionaries = dictionary?.let { listOf(it) } ?: ArucoDictionary.entries
    private val detectors = dictionaries.map { dictionary ->
        val parameters = DetectorParameters().apply {
            _cornerRefinementMethod = Objdetect.CORNER_REFINE_SUBPIX
            _errorCorrectionRate = dictionary.errorCorrectionRate
        }
        dictionary to ArucoDetector(
            Objdetect.getPredefinedDictionary(dictionary.toOpenCvDictionaryId()),
            parameters
        )
    }

    override fun processFrame(
        gray: Mat,
        color: Mat,
        timestampMs: Long,
        rotationDegrees: Int
    ): List<VisionEvent> {
        val calibrated = calibratedCameraModel
        val model = if (calibrated != null) {
            calibrated.takeIf { it.calibrated && it.matches(gray.cols(), gray.rows()) }
        } else {
            sensorGeometry?.estimate(gray.cols(), gray.rows())
        }
        val markers = mutableListOf<VisionEvent.MarkerDetected>()
        for ((dictionary, detector) in detectors) {
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
                    val markerRole = MarkerKey(dictionary, id).role()
                    val gridCells = dictionary.markerGridCells
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
                    val pose = model?.let {
                        solver.estimate(
                            offsets,
                            dictionary.sizeMm,
                            it,
                            markerRole,
                            gridCells
                        )
                    }
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
                            dictionary = dictionary,
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
                cameraParameters = when {
                    model == null -> CameraParameterSource.UNAVAILABLE
                    model.calibrated -> CameraParameterSource.CALIBRATED
                    else -> CameraParameterSource.ESTIMATED
                }
            )
        )
    }
}

private fun ArucoDictionary.toOpenCvDictionaryId(): Int = when (this) {
    ArucoDictionary.DICT_4X4_50 -> Objdetect.DICT_4X4_50
    ArucoDictionary.DICT_5X5_50 -> Objdetect.DICT_5X5_50
    ArucoDictionary.DICT_6X6_50 -> Objdetect.DICT_6X6_50
    ArucoDictionary.DICT_7X7_50 -> Objdetect.DICT_7X7_50
}
