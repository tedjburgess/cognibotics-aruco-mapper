package com.example.cogniboticsarucomapper.domain.measurement

import kotlin.math.abs
import kotlin.math.hypot

data class ImagePoint(val x: Double, val y: Double)

// Check how much image detail remains after perspective compresses a marker.
object MarkerImageQuality {
    fun thicknessPixels(points: List<ImagePoint>): Double {
        if (points.size != 4 || points.any { !it.x.isFinite() || !it.y.isFinite() }) return 0.0
        var twiceArea = 0.0
        var longestEdge = 0.0
        var winding = 0.0
        for (index in points.indices) {
            val a = points[index]
            val b = points[(index + 1) % 4]
            val c = points[(index + 2) % 4]
            val cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x)
            if (cross == 0.0 || (winding != 0.0 && cross * winding < 0.0)) return 0.0
            winding = cross
            twiceArea += a.x * b.y - a.y * b.x
            longestEdge = maxOf(longestEdge, hypot(b.x - a.x, b.y - a.y))
        }
        return if (longestEdge > 0.0) abs(twiceArea) / (2 * longestEdge) else 0.0
    }

    // Two pixels per printed cell is a visibility guard, not a physical accuracy guarantee.
    fun hasEnoughDetail(points: List<ImagePoint>, gridCells: Int): Boolean =
        gridCells in 6..9 && thicknessPixels(points) >= gridCells * 2.0
}
