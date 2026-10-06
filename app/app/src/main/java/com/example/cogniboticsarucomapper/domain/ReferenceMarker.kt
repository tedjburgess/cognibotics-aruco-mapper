package com.example.cogniboticsarucomapper.domain

data class ReferenceMarker(
    val dictionary: Int,
    val markerId: Int,
    val sizeMm: Double,
)

data class ArucoDictionaryOption(val code: Int, val label: String)

object ReferenceMarkerState {
    var value: ReferenceMarker? = null

    fun save(dictionary: Int, markerId: Int?, sizeMm: Double?) {
        if (markerId == null || sizeMm == null || sizeMm <= 0.0) {
            return
        }

        value = ReferenceMarker(
            dictionary = dictionary,
            markerId = markerId,
            sizeMm = sizeMm,
        )
    }
}
