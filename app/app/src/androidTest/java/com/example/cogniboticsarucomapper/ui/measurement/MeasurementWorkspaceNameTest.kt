package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeasurementWorkspaceNameTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun defaultSiteAndCellStartMeasurement() {
        var startedSite: String? = null
        var startedCell: String? = null

        compose.setContent {
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    onStartMeasurement = { site, cell ->
                        startedSite = site
                        startedCell = cell
                    },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Start scan")
            .assertIsEnabled()
            .performClick()

        compose.runOnIdle {
            assertEquals("Test Site", startedSite)
            assertEquals("Test Cell", startedCell)
        }
    }

    @Test
    fun emptySiteDisablesStart() {
        compose.setContent {
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    onStartMeasurement = { _, _ -> },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Test Site")
            .performTextClearance()

        compose.onNodeWithText("Start scan")
            .assertIsNotEnabled()
    }

    @Test
    fun emptyCellDisablesStart() {
        compose.setContent {
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    onStartMeasurement = { _, _ -> },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Test Cell")
            .performTextClearance()

        compose.onNodeWithText("Start scan")
            .assertIsNotEnabled()
    }
}
