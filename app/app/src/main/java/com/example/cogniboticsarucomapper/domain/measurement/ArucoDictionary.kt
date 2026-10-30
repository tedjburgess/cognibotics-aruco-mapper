package com.example.cogniboticsarucomapper.domain.measurement

enum class ArucoDictionary(
    val label: String,
    val sizeMm: Double,
    val markerGridCells: Int,
    val errorCorrectionRate: Double
) {
    DICT_4X4_50("4x4", 20.0, 6, 0.0),
    DICT_5X5_50("5x5", 50.0, 7, 0.4),
    DICT_6X6_50("6x6", 75.0, 8, 0.4),
    DICT_7X7_50("7x7", 100.0, 9, 0.4),

    // Project test size: the printed black square, including its one-cell border.
    DICT_ARUCO_ORIGINAL("Original", 110.0, 7, 0.0)
}
