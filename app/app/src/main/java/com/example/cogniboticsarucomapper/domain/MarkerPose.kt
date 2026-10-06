package com.example.cogniboticsarucomapper.domain

import kotlin.math.sqrt

data class MarkerPose(
    val tx: Double,
    val ty: Double,
    val tz: Double,
    val reprojectionErrorPx: Double,
) {
    val unitRangeMm: Double = sqrt(tx * tx + ty * ty + tz * tz)
}
