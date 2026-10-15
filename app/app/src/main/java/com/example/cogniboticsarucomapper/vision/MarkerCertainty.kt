package com.example.cogniboticsarucomapper.vision

import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey

data class MarkerCertainty(
    val key: MarkerKey,
    val consecutiveFrames: Int,
    val certainty: Float,
    val state: MarkerScanState
)

class MarkerCertaintyTracker(private val stableThreshold: Int = 10) {
    init {
        require(stableThreshold > 0)
    }

    private val frameCounts = mutableMapOf<MarkerKey, Int>()

    fun update(detectedKeys: Set<MarkerKey>): Map<MarkerKey, MarkerCertainty> {
        // These are consecutive observations, not accuracy probabilities.
        frameCounts.keys.retainAll(detectedKeys)
        for (key in detectedKeys) {
            frameCounts[key] = ((frameCounts[key] ?: 0) + 1).coerceAtMost(stableThreshold)
        }
        return frameCounts.mapValues { (key, frames) ->
            MarkerCertainty(
                key = key,
                consecutiveFrames = frames,
                certainty = frames.toFloat() / stableThreshold,
                state = when {
                    frames >= stableThreshold -> MarkerScanState.STABLE
                    frames >= stableThreshold / 2 -> MarkerScanState.CONFIRMING
                    else -> MarkerScanState.DETECTED
                }
            )
        }
    }
}
