package com.example.cogniboticsarucomapper.domain.measurement

import kotlin.math.sqrt

data class MarkerKey(val dictionary: ArucoDictionary, val id: Int)

/** Coordinates in the same camera frame, in millimetres. */

data class OrientationQuaternion(val x: Double, val y: Double, val z: Double, val w: Double)
data class PositionMm(val x: Double, val y: Double, val z: Double) {
    val isValid: Boolean get() = x.isFinite() && y.isFinite() && z.isFinite() && z > 0.0

    fun distanceTo(other: PositionMm): Double = sqrt(
        (x - other.x) * (x - other.x) +
            (y - other.y) * (y - other.y) +
            (z - other.z) * (z - other.z)
    )
}

data class PositionedMarker(val key: MarkerKey, val position: PositionMm)

data class MarkerPairDistance(val from: MarkerKey, val to: MarkerKey, val distanceMm: Double)

object MarkerMeasurements {
    /** Never mix frames or infer the scale of one marker from another marker's plane. */
    fun distances(markers: List<PositionedMarker>): List<MarkerPairDistance> {
        val unique = markers.groupBy { it.key }.values
            .filter { it.size == 1 }
            .map { it.single() }
            .filter { it.key.id >= 0 && it.position.isValid }
            .sortedWith(compareBy({ it.key.dictionary.ordinal }, { it.key.id }))
        return buildList {
            for (left in unique.indices) {
                for (right in left + 1 until unique.size) {
                    val distance = unique[left].position.distanceTo(unique[right].position)
                    if (distance.isFinite()) {
                        add(MarkerPairDistance(unique[left].key, unique[right].key, distance))
                    }
                }
            }
        }
    }
}
