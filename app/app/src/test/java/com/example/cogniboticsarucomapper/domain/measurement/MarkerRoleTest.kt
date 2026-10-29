package com.example.cogniboticsarucomapper.domain.measurement

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkerRoleTest {

    @Test
    fun `ids 0 to 9 are directional for every dictionary`() {
        for (dictionary in ArucoDictionary.entries) {
            for (id in 0..9) {
                val key = MarkerKey(dictionary, id)

                assertEquals(
                    "Expected $dictionary marker $id to be directional",
                    MarkerOrigin.CORNER,
                    key.role()
                )
            }
        }
    }

    @Test
    fun `id 10 and above are positional for every dictionary`() {
        for (dictionary in ArucoDictionary.entries) {
            val idsToCheck = listOf(10, 20, 49)

            for (id in idsToCheck) {
                val key = MarkerKey(dictionary, id)

                assertEquals(
                    "Expected $dictionary marker $id to be positional",
                    MarkerOrigin.CENTER,
                    key.role()
                )
            }
        }
    }

    @Test
    fun `boundary between directional and positional is correct`() {
        for (dictionary in ArucoDictionary.entries) {
            val directional = MarkerKey(dictionary, 9)
            val positional = MarkerKey(dictionary, 10)

            assertEquals(
                MarkerOrigin.CORNER,
                directional.role()
            )

            assertEquals(
                MarkerOrigin.CENTER,
                positional.role()
            )
        }
    }
}
