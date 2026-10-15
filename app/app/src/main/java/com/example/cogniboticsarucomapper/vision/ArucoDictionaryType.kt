package com.example.cogniboticsarucomapper.vision

import com.example.cogniboticsarucomapper.domain.measurement.MarkerFamily
import org.opencv.objdetect.Objdetect

// Capacity changes the available IDs; physical size still comes from the marker family.
enum class ArucoDictionaryType(val openCvId: Int, val family: MarkerFamily) {
    DICT_4X4_50(Objdetect.DICT_4X4_50, MarkerFamily.FOUR_BY_FOUR),
    DICT_5X5_50(Objdetect.DICT_5X5_50, MarkerFamily.FIVE_BY_FIVE),
    DICT_6X6_50(Objdetect.DICT_6X6_50, MarkerFamily.SIX_BY_SIX),
    DICT_7X7_50(Objdetect.DICT_7X7_50, MarkerFamily.SEVEN_BY_SEVEN),
    DICT_4X4_1000(Objdetect.DICT_4X4_1000, MarkerFamily.FOUR_BY_FOUR),
    DICT_5X5_1000(Objdetect.DICT_5X5_1000, MarkerFamily.FIVE_BY_FIVE),
    DICT_6X6_1000(Objdetect.DICT_6X6_1000, MarkerFamily.SIX_BY_SIX),
    DICT_7X7_1000(Objdetect.DICT_7X7_1000, MarkerFamily.SEVEN_BY_SEVEN)
}
