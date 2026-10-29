package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import com.example.cogniboticsarucomapper.domain.measurement.MarkerOrigin
import com.example.cogniboticsarucomapper.domain.measurement.MarkerPairDistance
import com.example.cogniboticsarucomapper.domain.measurement.PositionMm
import com.example.cogniboticsarucomapper.domain.measurement.role

enum class CameraParameterSource { UNAVAILABLE, ESTIMATED, CALIBRATED }

sealed interface VisionEvent {
    data class MarkerDetected(
        val markerId: Int,
        val corners: List<Offset>,
        val center: Offset,
        val timestampMs: Long,
        val certainty: Float = 0f,
        val scanState: MarkerScanState = MarkerScanState.DETECTED,
        val frameWidth: Int,
        val frameHeight: Int,
        val rotationDegrees: Int,
        val dictionary: ArucoDictionary = ArucoDictionary.DICT_4X4_50,
        val positionMm: PositionMm? = null,
        val tiltDegrees: Double? = null
    ) : VisionEvent {

        val key: MarkerKey
            get() = MarkerKey(dictionary, markerId)

        val role: MarkerOrigin
            get() = key.role()
    }

    /** A complete single-frame snapshot, including an empty snapshot when nothing is visible. */
    data class FrameMeasured(
        val markers: List<MarkerDetected>,
        val distances: List<MarkerPairDistance>,
        val cameraParameters: CameraParameterSource
    ) : VisionEvent

    data class MarkersCleared(val timestampMs: Long) : VisionEvent

    data class AnalysisError(val message: String, val throwable: Throwable? = null) : VisionEvent
}
