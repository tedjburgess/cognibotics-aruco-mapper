package com.example.cogniboticsarucomapper.data

import com.example.cogniboticsarucomapper.domain.ArucoDictionaryOption
import com.example.cogniboticsarucomapper.domain.MarkerDetection
import com.example.cogniboticsarucomapper.domain.MeasurementEngine
import com.example.cogniboticsarucomapper.domain.ReferenceMarker
import com.example.cogniboticsarucomapper.domain.VisionEvent
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect

class ArucoAnalyzer(
    private val sensorGeometry: SensorGeometry? = null,
    @Volatile var reference: ReferenceMarker? = null,
    enabledDictionaries: List<ArucoDictionaryOption> = ACTIVE_DICTIONARIES,
) : VisionEngine.FrameAnalyzer {
    private val detectorParameters = DetectorParameters()
    private val poseSolver = PoseSolver()
    private val dictionaryDetectors: List<MarkerDetector>

    private var cachedIntrinsics: CameraIntrinsics? = null
    private var cachedIntrinsicsWidth = 0
    private var cachedIntrinsicsHeight = 0

    init {
        detectorParameters._useAruco3Detection = true
        detectorParameters._cornerRefinementMethod = Objdetect.CORNER_REFINE_APRILTAG
        detectorParameters._minDistanceToBorder = 4
        detectorParameters._minMarkerDistanceRate = 0.04

        val detectors = ArrayList<MarkerDetector>()
        for (option in enabledDictionaries) {
            detectors.add(MarkerDetector(option.code, detectorParameters))
        }

        dictionaryDetectors = detectors
    }

    override fun processFrame(gray: Mat): List<VisionEvent> {
        val intrinsics = intrinsicsFor(gray.width(), gray.height())
            ?: return listOf(VisionEvent.AnalysisError("Camera intrinsics unavailable for this frame"))

        val cameraMatrix = intrinsics.cameraMatrix()
        val distortionCoefficients = intrinsics.distortionCoefficients()

        return try {
            measureFrame(gray, cameraMatrix, distortionCoefficients)
        } catch (exception: Exception) {
            listOf(VisionEvent.AnalysisError.from(exception))
        } finally {
            cameraMatrix.release()
            distortionCoefficients.release()
        }
    }

    private fun measureFrame(
        gray: Mat,
        cameraMatrix: Mat,
        distortionCoefficients: MatOfDouble,
    ): List<VisionEvent> {
        val detections = ArrayList<MarkerDetection>()
        var sawMarker = false
        var failure: Exception? = null

        for (markerDetector in dictionaryDetectors) {
            try {
                val found = markerDetector.detect(gray, poseSolver, cameraMatrix, distortionCoefficients)
                if (found.isNotEmpty()) {
                    sawMarker = true
                }
                detections.addAll(found)
            } catch (exception: Exception) {
                if (failure == null) {
                    failure = exception
                }
            }
        }

        if (detections.isNotEmpty()) {
            val measurement = MeasurementEngine.measure(reference, detections)
            return listOf(VisionEvent.MeasurementUpdated(measurement))
        }

        if (failure != null) {
            return listOf(VisionEvent.AnalysisError.from(failure))
        }

        if (sawMarker) {
            return listOf(VisionEvent.AnalysisError("Markers detected but no pose could be solved"))
        }

        return listOf(VisionEvent.MarkersCleared)
    }

    private fun intrinsicsFor(width: Int, height: Int): CameraIntrinsics? {
        if (width == cachedIntrinsicsWidth && height == cachedIntrinsicsHeight) {
            return cachedIntrinsics
        }

        val intrinsics = CameraIntrinsics.fromSensor(sensorGeometry, width, height) ?: return null

        cachedIntrinsics = intrinsics
        cachedIntrinsicsWidth = width
        cachedIntrinsicsHeight = height
        
        return intrinsics
    }
}
