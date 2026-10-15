package com.example.cogniboticsarucomapper.domain.calibration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckerboardConfigTest {
    @Test
    fun acceptsMeasuredSquareSizeAndInnerCornerCounts() {
        assertEquals(CheckerboardConfig(9, 6, 25.5), CheckerboardConfig.parse(" 9 ", "6", "25.5"))
    }

    @Test
    fun rejectsMissingOrUnsafeBoardParameters() {
        for (columns in listOf("", "0", "1", "31", "2.5")) {
            assertNull(CheckerboardConfig.parse(columns, "6", "25"))
        }
        for (size in listOf("", "0", "-1", "NaN", "Infinity", "word")) {
            assertNull(CheckerboardConfig.parse("9", "6", size))
        }
        assertNull(CheckerboardConfig.parse("9", "1", "25"))
    }
}
