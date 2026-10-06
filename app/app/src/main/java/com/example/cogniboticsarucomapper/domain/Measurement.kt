package com.example.cogniboticsarucomapper.domain

import kotlin.math.sqrt

data class Point3d(val x: Double, val y: Double, val z: Double) {
    fun distanceTo(other: Point3d): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

data class MarkerKey(val dictionary: Int, val markerId: Int)

data class MarkerDetection(
    val dictionary: Int,
    val markerId: Int,
    val pose: MarkerPose,
    val edgeLengthPx: Double,
)

data class MarkerSize(
    val dictionary: Int,
    val markerId: Int,
    val sizeMm: Double,
    val edgeLengthPx: Double,
)

data class MarkerPairDistance(
    val from: MarkerKey,
    val to: MarkerKey,
    val distanceMm: Double,
)

data class FrameMeasurement(
    val detectedCount: Int,
    val referenceVisible: Boolean,
    val sizes: List<MarkerSize>,
    val distances: List<MarkerPairDistance>,
)
