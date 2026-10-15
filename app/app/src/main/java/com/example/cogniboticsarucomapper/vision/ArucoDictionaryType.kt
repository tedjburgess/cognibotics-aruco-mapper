package com.example.cogniboticsarucomapper.vision

import org.opencv.objdetect.Objdetect

enum class ArucoDictionaryType(val openCvId: Int) {
    DICT_4X4_50(Objdetect.DICT_4X4_50),
    DICT_5X5_50(Objdetect.DICT_5X5_50),
    DICT_6X6_50(Objdetect.DICT_6X6_50),
    DICT_7X7_50(Objdetect.DICT_7X7_50)
}
