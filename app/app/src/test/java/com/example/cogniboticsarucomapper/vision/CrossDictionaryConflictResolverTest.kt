package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossDictionaryConflictResolverTest {
    private val four = marker(ArucoDictionary.DICT_4X4_50, 17)
    private val seven = marker(ArucoDictionary.DICT_7X7_50, 44)

    @Test fun choosesTheFullPatternInsteadOfTheCoarserAlias() {
        val result = CrossDictionaryConflictResolver.resolve(listOf(four, seven)) {
            if (it.dictionary == seven.dictionary) 0.0 else 0.21
        }
        assertEquals(listOf(seven), result)
    }

    @Test fun closeOrPoorFitsProduceNoInventedIdentity() {
        for (errors in listOf(0.01 to 0.03, 0.3 to 0.5)) {
            assertTrue(
                CrossDictionaryConflictResolver.resolve(listOf(four, seven)) {
                    if (it.dictionary == four.dictionary) errors.first else errors.second
                }.isEmpty()
            )
        }
    }

    @Test fun theFinerDictionaryDoesNotWinJustBecauseItHasMoreCells() {
        assertEquals(
            listOf(four),
            CrossDictionaryConflictResolver.resolve(listOf(four, seven)) {
                if (it.dictionary == four.dictionary) 0.0 else 0.21
            }
        )
    }

    @Test fun separateMarkersAndRepeatedIdsAreNotRemoved() {
        val separate = seven.copy(corners = seven.corners.map { it + Offset(200f, 0f) })
        val sameId = four.copy(corners = four.corners.map { it + Offset(400f, 0f) })
        val input = listOf(four, separate, sameId)
        assertEquals(
            input,
            CrossDictionaryConflictResolver.resolve(input) {
                error("No conflict to score")
            }
        )
    }

    @Test fun cornerOrderDoesNotHideAConflictingDictionary() {
        val rotatedOrder = seven.copy(corners = seven.corners.drop(1) + seven.corners.take(1))
        assertEquals(
            listOf(rotatedOrder),
            CrossDictionaryConflictResolver.resolve(listOf(four, rotatedOrder)) {
                if (it.dictionary == seven.dictionary) 0.0 else 0.21
            }
        )
    }

    private fun marker(dictionary: ArucoDictionary, id: Int) = VisionEvent.MarkerDetected(
        markerId = id,
        corners = listOf(Offset(0f, 0f), Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f)),
        center = Offset(50f, 50f),
        timestampMs = 0,
        frameWidth = 640,
        frameHeight = 480,
        rotationDegrees = 0,
        dictionary = dictionary
    )
}
