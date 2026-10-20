package com.example.cogniboticsarucomapper.util

import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import java.util.Locale

object MeasurementFormats {
    private const val MILLIMETER_UNIT = "mm"

    fun markerLabel(dictionary: ArucoDictionary, id: Int): String = "${dictionary.label} #$id"

    fun markerPairLabel(from: MarkerKey, to: MarkerKey): String =
        "${markerLabel(from.dictionary, from.id)} ↔ ${markerLabel(to.dictionary, to.id)}"

    fun historyMarkerLabel(id: Int, dictionary: String): String = "$dictionary #$id"

    fun millimeters(value: Double, decimalPlaces: Int): String =
        String.format(Locale.getDefault(), "%.${decimalPlaces}f $MILLIMETER_UNIT", value)
}
