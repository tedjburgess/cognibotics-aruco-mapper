package com.example.cogniboticsarucomapper.ui.measurement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceNameTest {
    @Test
    fun trimsWhitespaceWithoutChangingInternalSpacing() {
        assertEquals("Assembly Cell  12", normalizeWorkspaceName("  Assembly Cell  12  "))
    }

    @Test
    fun rejectsBlankAndWhitespaceOnlyNames() {
        assertNull(normalizeWorkspaceName(""))
        assertNull(normalizeWorkspaceName("  \t  "))
    }

    @Test
    fun acceptsMaximumLengthButRejectsLongerNames() {
        assertEquals("A".repeat(255), normalizeWorkspaceName("A".repeat(255)))
        assertNull(normalizeWorkspaceName("A".repeat(256)))
    }

    @Test
    fun supportsNonEnglishWorkspaceNames() {
        assertEquals("Mätcell Östra", normalizeWorkspaceName("  Mätcell Östra "))
    }
}
