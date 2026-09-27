package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

sealed interface VisionEvent {
    data class MarkerDetected(
        val markerId: Int,
        val corners: List<Offset>,
        val center: Offset,
        val timestampMs: Long
    ) : VisionEvent

    data class MarkersCleared(val timestampMs: Long) : VisionEvent

    data class AnalysisError(val message: String, val throwable: Throwable? = null) : VisionEvent
}