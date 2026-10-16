package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.domain.measurement.MarkerFamily
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import com.example.cogniboticsarucomapper.domain.measurement.MarkerOrientation
import com.example.cogniboticsarucomapper.domain.measurement.MarkerPairDistance
import com.example.cogniboticsarucomapper.domain.measurement.PositionMm

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
        val family: MarkerFamily = MarkerFamily.FOUR_BY_FOUR,
        val positionMm: PositionMm? = null,
        val tiltDegrees: Double? = null,
        val orientation: MarkerOrientation? = null
    ) : VisionEvent {
        val key: MarkerKey get() = MarkerKey(family, markerId)
    }

    /** A complete single-frame snapshot, including an empty snapshot when nothing is visible. */
    data class FrameMeasured(
        val markers: List<MarkerDetected>,
        val distances: List<MarkerPairDistance>,
        val cameraParameters: CameraParameterSource,
        val testSheetMode: Boolean = true
    ) : VisionEvent

    data class MarkersCleared(val timestampMs: Long) : VisionEvent

    data class AnalysisError(val message: String, val throwable: Throwable? = null) : VisionEvent
}
