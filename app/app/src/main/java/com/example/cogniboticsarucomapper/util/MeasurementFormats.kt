package com.example.cogniboticsarucomapper.util

import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import java.util.Locale

object MeasurementFormats {
    private const val MILLIMETER_UNIT = "mm"

    fun markerLabel(dictionary: ArucoDictionary, id: Int): String = "${dictionary.label} #$id"

    fun historyMarkerLabel(id: Int, dictionary: String): String = "ID $id - $dictionary"

    fun millimeters(value: Double, decimalPlaces: Int): String =
        String.format(Locale.getDefault(), "%.${decimalPlaces}f $MILLIMETER_UNIT", value)

    fun percentage(value: Double, decimalPlaces: Int): String =
        String.format(Locale.getDefault(), "%.${decimalPlaces}f%%", value)
}
