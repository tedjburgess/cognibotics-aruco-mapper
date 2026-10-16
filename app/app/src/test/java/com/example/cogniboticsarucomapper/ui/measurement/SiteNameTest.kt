package com.example.cogniboticsarucomapper.ui.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SiteNameTest {
    @Test
    fun trimsRequiredSiteName() {
        assertEquals("Factory north", normalizeSiteName("  Factory north  "))
    }

    @Test
    fun rejectsBlankAndTooLongSites() {
        assertNull(normalizeSiteName("  "))
        assertNull(normalizeSiteName("x".repeat(256)))
        assertEquals("x".repeat(255), normalizeSiteName("x".repeat(255)))
    }
}
