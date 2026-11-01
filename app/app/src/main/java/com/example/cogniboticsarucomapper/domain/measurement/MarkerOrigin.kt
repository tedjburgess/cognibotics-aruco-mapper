package com.example.cogniboticsarucomapper.domain.measurement

enum class MarkerOrigin {
    CORNER,
    CENTER
}

fun MarkerKey.role(): MarkerOrigin = if (id in 0..9) {
    MarkerOrigin.CORNER
} else {
    MarkerOrigin.CENTER
}
