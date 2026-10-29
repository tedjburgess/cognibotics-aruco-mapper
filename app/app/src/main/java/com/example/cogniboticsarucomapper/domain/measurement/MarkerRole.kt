package com.example.cogniboticsarucomapper.domain.measurement

enum class MarkerRole {
    DIRECTIONAL_REFERENCE,
    POSITIONAL
}

fun MarkerKey.role(): MarkerRole = if (id in 0..9) {
    MarkerRole.DIRECTIONAL_REFERENCE
} else {
    MarkerRole.POSITIONAL
}
