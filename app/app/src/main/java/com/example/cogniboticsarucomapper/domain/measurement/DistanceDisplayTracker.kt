package com.example.cogniboticsarucomapper.domain.measurement

import kotlin.math.abs

data class PairReading(
    val from: MarkerKey,
    val to: MarkerKey,
    val rawMm: Double?,
    val steadyMm: Double?
) {
    // Only the named pairs on our flat test sheets have a known 135 mm reference.
    val isTestSheetPair: Boolean get() = from.family == to.family &&
        ((from.id == 0 && to.id == 1) || (from.id == 2 && to.id == 3))
    val absoluteTestErrorMm: Double? get() = rawMm?.let { abs(it - 135.0) }
}

// Keep pair rows in a fixed order; an unavailable current reading must never reuse an old number.
class DistanceDisplayTracker {
    private data class PairKey(val from: MarkerKey, val to: MarkerKey)
    private data class Sample(val value: Double, val timeMs: Long)
    private val known = mutableMapOf<PairKey, Long>()
    private val rowRetentionMs = 1_500L
    private val windows = mutableMapOf<PairKey, ArrayDeque<Sample>>()
    private var lastTimeMs = Long.MIN_VALUE

    fun update(
        keys: Set<MarkerKey>,
        distances: List<MarkerPairDistance>,
        timeMs: Long
    ): List<PairReading> {
        if (timeMs < lastTimeMs) {
            windows.clear()
            known.clear()
        }
        lastTimeMs = timeMs
        known.entries.removeAll { (_, lastSeen) -> timeMs - lastSeen > rowRetentionMs }
        val sorted = keys.sortedWith(compareBy({ it.family.ordinal }, { it.id }))
        for (left in sorted.indices) {
            for (right in left + 1 until sorted.size) {
                known[PairKey(sorted[left], sorted[right])] = timeMs
            }
        }
        val current = distances.filter { it.distanceMm.isFinite() && it.distanceMm >= 0.0 }
            .associateBy { PairKey(it.from, it.to) }
        windows.keys.retainAll(current.keys)
        for ((pair, measurement) in current) {
            known[pair] = timeMs
            val window = windows.getOrPut(pair) { ArrayDeque() }
            while (window.isNotEmpty() && timeMs - window.first().timeMs > 500) window.removeFirst()
            window.addLast(Sample(measurement.distanceMm, timeMs))
            while (window.size > 5) window.removeFirst()
        }
        return known.keys.sortedWith(
            compareBy(
                { it.from.family.ordinal },
                { it.from.id },
                { it.to.family.ordinal },
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

    // Starting a new sheet/session clears the roster and all smoothing history.
    fun reset() {
        known.clear()
        windows.clear()
        lastTimeMs = Long.MIN_VALUE
    }
}
