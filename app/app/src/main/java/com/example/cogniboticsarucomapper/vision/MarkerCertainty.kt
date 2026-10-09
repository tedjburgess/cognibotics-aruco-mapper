package com.example.cogniboticsarucomapper.vision

data class MarkerCertainty(
    val markerId: Int,
    val consecutiveFrames: Int,
    val certainty: Float,
    val state: MarkerScanState
)

class MarkerCertaintyTracker(
    private val stableThreshold: Int = 10
) {

    private val frameCounts = mutableMapOf<Int, Int>()

    fun update(
        detectedIds: Set<Int>
    ): Map<Int, MarkerCertainty> {

        val allKnownIds = frameCounts.keys union detectedIds

        allKnownIds.forEach { id ->

            if (id in detectedIds) {
                frameCounts[id] =
                    ((frameCounts[id] ?: 0) + 1)
                        .coerceAtMost(stableThreshold)
            } else {
                frameCounts[id] =
                    ((frameCounts[id] ?: 0) - 1)
                        .coerceAtLeast(0)
            }
        }

        return frameCounts.mapValues { (id, frames) ->

            val certainty =
                (frames.toFloat() / stableThreshold)
                    .coerceIn(0f, 1f)

            val state = when {
                frames >= stableThreshold ->
                    MarkerScanState.STABLE

                frames >= stableThreshold / 2 ->
                    MarkerScanState.CONFIRMING

                else ->
                    MarkerScanState.DETECTED
            }

            MarkerCertainty(
                markerId = id,
                consecutiveFrames = frames,
                certainty = certainty,
                state = state
            )
        }
    }
}