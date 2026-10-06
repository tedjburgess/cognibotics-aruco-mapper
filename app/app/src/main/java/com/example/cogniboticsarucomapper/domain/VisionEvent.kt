package com.example.cogniboticsarucomapper.domain

sealed interface VisionEvent {
    data class MeasurementUpdated(val measurement: FrameMeasurement) : VisionEvent
    object MarkersCleared : VisionEvent
    data class AnalysisError(val message: String) : VisionEvent {
        companion object {
            fun from(throwable: Throwable): AnalysisError {
                val detail = throwable.message ?: "unknown error"
                return AnalysisError("${throwable.javaClass.simpleName}: $detail")
            }
        }
    }
}
