package com.example.cogniboticsarucomapper.vision

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

// Different dictionaries can decode the same square. Compare the full pattern before choosing its size.
internal object CrossDictionaryConflictResolver {
    private const val MAX_PATTERN_ERROR = 0.15
    private const val MIN_ERROR_GAP = 0.05

    fun resolve(
        markers: List<VisionEvent.MarkerDetected>,
        patternError: (VisionEvent.MarkerDetected) -> Double
    ): List<VisionEvent.MarkerDetected> {
        val visited = mutableSetOf<Int>()
        val kept = markers.indices.toMutableSet()
        for (index in markers.indices) {
            if (!visited.add(index)) continue
            val group = mutableListOf(index)
            var next = 0
            while (next < group.size) {
                val current = group[next++]
                for (other in markers.indices) {
                    if (other !in visited &&
                        markers[current].dictionary != markers[other].dictionary &&
                        sameSquare(markers[current], markers[other])
                    ) {
                        visited.add(other)
                        group.add(other)
                    }
                }
            }
            if (group.size == 1) continue
            val scored = group.map { it to patternError(markers[it]) }.sortedBy { it.second }
            kept.removeAll(group.toSet())
            val best = scored[0]
            // An uncertain interpretation must not become a confidently wrong physical measurement.
            if (best.second.isFinite() &&
                best.second <= MAX_PATTERN_ERROR &&
                scored[1].second - best.second >= MIN_ERROR_GAP
            ) {
                kept.add(best.first)
            }
        }
        return markers.filterIndexed { index, _ -> index in kept }
    }

    private fun sameSquare(a: VisionEvent.MarkerDetected, b: VisionEvent.MarkerDetected): Boolean {
        if (a.corners.size != 4 || b.corners.size != 4) return false
        fun area(marker: VisionEvent.MarkerDetected): Double = kotlin.math.abs(
            marker.corners.indices.sumOf { i ->
                val p = marker.corners[i]
                val q = marker.corners[(i + 1) % 4]
                p.x.toDouble() * q.y - q.x.toDouble() * p.y
            }
        ) / 2
        val tolerance = max(2.0, sqrt(max(area(a), area(b))) * 0.03)
        // Decoded corner order can differ by quarter turns between the conflicting dictionaries.
        return a.corners.all { p ->
            b.corners.any { q ->
                hypot((p.x - q.x).toDouble(), (p.y - q.y).toDouble()) <= tolerance
            }
        } &&
            b.corners.all { p ->
                a.corners.any { q ->
                    hypot((p.x - q.x).toDouble(), (p.y - q.y).toDouble()) <= tolerance
                }
            }
    }
}
