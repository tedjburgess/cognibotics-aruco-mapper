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
    fun requiresNameAndPassesTrimmedNameWhenStartingMeasurement() {
        var startedWith: String? = null
        compose.setContent {
            var workspaceName by remember { mutableStateOf("") }
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    workspaceName = workspaceName,
                    onWorkspaceNameChange = { workspaceName = it },
                    onStartMeasurement = { startedWith = it },
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Start Measurement").assertIsNotEnabled()
        compose.onNodeWithText("Cell / workspace name")
            .performTextInput("  Assembly cell A  ")
        compose.onNodeWithText("Start Measurement").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("Assembly cell A", startedWith) }
    }

    @Test
    fun whitespaceOnlyNameDoesNotEnableStart() {
        compose.setContent {
            var workspaceName by remember { mutableStateOf("") }
            CogniboticsArucoMapperTheme {
                MeasurementScreen(
                    workspaceName = workspaceName,
                    onWorkspaceNameChange = { workspaceName = it },
                    onStartMeasurement = {},
                    onHistoryClick = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Cell / workspace name").performTextInput("    ")
        compose.onNodeWithText("Start Measurement").assertIsNotEnabled()
    }
}
