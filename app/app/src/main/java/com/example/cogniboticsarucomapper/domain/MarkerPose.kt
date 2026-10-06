package com.example.cogniboticsarucomapper.domain

import kotlin.math.sqrt

data class MarkerPose(
    val tx: Double,
    val ty: Double,
    val tz: Double,
    val normalX: Double,
    val normalY: Double,
    val normalZ: Double,
    val reprojectionErrorPx: Double,
) {
    val unitRangeMm: Double = sqrt(tx * tx + ty * ty + tz * tz)

    fun positionMm(sizeMm: Double): Point3d {
        return Point3d(
            x = tx * sizeMm,
            y = ty * sizeMm,
            z = tz * sizeMm,
        )
    }
}
