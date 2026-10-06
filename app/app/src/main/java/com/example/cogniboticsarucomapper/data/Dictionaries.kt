package com.example.cogniboticsarucomapper.data

import com.example.cogniboticsarucomapper.domain.ArucoDictionaryOption
import org.opencv.objdetect.Objdetect

const val DEFAULT_REFERENCE_MARKER_DICTIONARY = Objdetect.DICT_4X4_50

val DICTIONARIES: List<ArucoDictionaryOption> = listOf(
    ArucoDictionaryOption(Objdetect.DICT_4X4_50, "DICT_4X4_50"),
    ArucoDictionaryOption(Objdetect.DICT_4X4_100, "DICT_4X4_100"),
    ArucoDictionaryOption(Objdetect.DICT_4X4_250, "DICT_4X4_250"),
    ArucoDictionaryOption(Objdetect.DICT_4X4_1000, "DICT_4X4_1000"),
    ArucoDictionaryOption(Objdetect.DICT_5X5_50, "DICT_5X5_50"),
    ArucoDictionaryOption(Objdetect.DICT_5X5_100, "DICT_5X5_100"),
    ArucoDictionaryOption(Objdetect.DICT_5X5_250, "DICT_5X5_250"),
    ArucoDictionaryOption(Objdetect.DICT_5X5_1000, "DICT_5X5_1000"),
    ArucoDictionaryOption(Objdetect.DICT_6X6_50, "DICT_6X6_50"),
    ArucoDictionaryOption(Objdetect.DICT_6X6_100, "DICT_6X6_100"),
    ArucoDictionaryOption(Objdetect.DICT_6X6_250, "DICT_6X6_250"),
    ArucoDictionaryOption(Objdetect.DICT_6X6_1000, "DICT_6X6_1000"),
    ArucoDictionaryOption(Objdetect.DICT_7X7_50, "DICT_7X7_50"),
    ArucoDictionaryOption(Objdetect.DICT_7X7_100, "DICT_7X7_100"),
    ArucoDictionaryOption(Objdetect.DICT_7X7_250, "DICT_7X7_250"),
    ArucoDictionaryOption(Objdetect.DICT_7X7_1000, "DICT_7X7_1000"),
    ArucoDictionaryOption(Objdetect.DICT_ARUCO_ORIGINAL, "DICT_ARUCO_ORIGINAL"),
)

val SUPPORTED_SCAN_DICTIONARIES: List<ArucoDictionaryOption> =
    DICTIONARIES.filter { option -> option.code == Objdetect.DICT_4X4_50 }

fun dictionaryLabel(code: Int): String {
    val match = DICTIONARIES.firstOrNull { option -> option.code == code }
    if (match == null) {
        return "DICT($code)"
    }

    return match.label
}
