package com.example.cogniboticsarucomapper.domain.calibration

// Count the intersections inside the board, not the number of printed squares.
data class CheckerboardConfig(val columns: Int, val rows: Int, val squareSizeMm: Double) {
    companion object {
        fun parse(columns: String, rows: String, squareSizeMm: String): CheckerboardConfig? {
            val width = columns.trim().toIntOrNull() ?: return null
            val height = rows.trim().toIntOrNull() ?: return null
            val size = squareSizeMm.trim().toDoubleOrNull() ?: return null
            if (width !in 2..30 || height !in 2..30 || !size.isFinite() || size <= 0) return null
            return CheckerboardConfig(width, height, size)
        }
    }
}
