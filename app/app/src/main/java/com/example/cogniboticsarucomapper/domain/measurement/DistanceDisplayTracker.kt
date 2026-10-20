package com.example.cogniboticsarucomapper.domain.measurement

data class PairReading(
    val from: MarkerKey,
    val to: MarkerKey,
    val rawMm: Double?,
    val steadyMm: Double?
)

// Keep pair rows in a fixed order; an unavailable current reading must never reuse an old number.
class DistanceDisplayTracker {
    private data class PairKey(val from: MarkerKey, val to: MarkerKey)
    private data class Sample(val value: Double, val timeMs: Long)
    private val known = mutableSetOf<PairKey>()
    private val windows = mutableMapOf<PairKey, ArrayDeque<Sample>>()
    private var lastTimeMs = Long.MIN_VALUE

    fun update(
        keys: Set<MarkerKey>,
        distances: List<MarkerPairDistance>,
        timeMs: Long
    ): List<PairReading> {
        if (timeMs < lastTimeMs) windows.clear()
        lastTimeMs = timeMs
        val sorted = keys.sortedWith(compareBy({ it.dictionary.ordinal }, { it.id }))
        for (left in sorted.indices) {
            for (right in left + 1 until sorted.size) {
                known.add(
                    PairKey(sorted[left], sorted[right])
                )
            }
        }
        val current = distances.filter { it.distanceMm.isFinite() && it.distanceMm >= 0.0 }
            .associateBy { PairKey(it.from, it.to) }
        windows.keys.retainAll(current.keys)
        for ((pair, measurement) in current) {
            known.add(pair)
            val window = windows.getOrPut(pair) { ArrayDeque() }
            while (window.isNotEmpty() && timeMs - window.first().timeMs > 500) window.removeFirst()
            window.addLast(Sample(measurement.distanceMm, timeMs))
            while (window.size > 5) window.removeFirst()
        }
        return known.sortedWith(
            compareBy(
                { it.from.dictionary.ordinal },
                { it.from.id },
                { it.to.dictionary.ordinal },
                { it.to.id }
            )
        ).map { pair ->
            val values = windows[pair]?.map { it.value }?.sorted().orEmpty()
            val median = when {
                values.isEmpty() -> null
                values.size % 2 == 1 -> values[values.size / 2]
                else -> (values[values.size / 2 - 1] + values[values.size / 2]) / 2
            }
            PairReading(pair.from, pair.to, current[pair]?.distanceMm, median)
        }
    }

    // Starting a new session clears the roster and all smoothing history.
    fun reset() {
        known.clear()
        windows.clear()
        lastTimeMs = Long.MIN_VALUE
    }
}
