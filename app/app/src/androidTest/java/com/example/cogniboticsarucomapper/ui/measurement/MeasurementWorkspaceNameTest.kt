package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
    fun requiresBothNamesAndPassesTrimmedMetadata() {
        var site: String? = null
        var cell: String? = null
        compose.setContent {
            var siteName by remember { mutableStateOf("") }
            var workspaceName by remember { mutableStateOf("") }
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    siteName = siteName,
                    onSiteNameChange = { siteName = it },
                    workspaceName = workspaceName,
                    onWorkspaceNameChange = { workspaceName = it },
                    onStartMeasurement = { selectedSite, selectedCell ->
                        site = selectedSite
                        cell = selectedCell
                    },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Start Measurement").assertIsNotEnabled()
        compose.onNodeWithText("Cell / workspace name").performTextInput("  Assembly cell A  ")
        compose.onNodeWithText("Start Measurement").assertIsNotEnabled()
        compose.onNodeWithText("Site name").performTextInput("  Test lab  ")
        compose.onNodeWithText("Start Measurement").assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals("Test lab", site)
            assertEquals("Assembly cell A", cell)
        }
    }

    @Test
    fun whitespaceOnlySiteDoesNotEnableStart() {
        compose.setContent {
            var siteName by remember { mutableStateOf("") }
            var workspaceName by remember { mutableStateOf("Cell A") }
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    siteName = siteName,
                    onSiteNameChange = { siteName = it },
                    workspaceName = workspaceName,
                    onWorkspaceNameChange = { workspaceName = it },
                    onStartMeasurement = { _, _ -> },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Site name").performTextInput("   ")
        compose.onNodeWithText("Start Measurement").assertIsNotEnabled()
    }
}
