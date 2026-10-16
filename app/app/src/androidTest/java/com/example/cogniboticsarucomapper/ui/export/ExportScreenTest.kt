package com.example.cogniboticsarucomapper.ui.export

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.network.model.ObservationRequest
import com.example.cogniboticsarucomapper.network.model.OrientationDto
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withoutCapturedDataOffersMeasurementInsteadOfDownload() {
        compose.setContent {
            CogniboticsArucoMapperTheme {
                ExportScreen(
                    scan = null,
                    savedScanId = null,
                    onSave = { error("No scan") },
                    onSaved = {},
                    onStartMeasurement = {},
                    onBack = {}
                )
            }
        }
        compose.onNodeWithText("No measurement captured").assertExists()
        compose.onNodeWithText("Save measurement to database").assertDoesNotExist()
    }

    @Test
    fun submitsCurrentApiPayloadAndShowsDatabaseConfirmation() {
        val request = ScanRequest(
            site_name = "Factory",
            cell_name = "Cell A",
            device_id = "Android",
            measured_at = "2026-10-16T01:00:00Z",
            observations = listOf(
                ObservationRequest(
                    aruco_id = 1,
                    dictionary = "DICT_6X6_50",
                    position = PositionDto(1.0, 2.0, 500.0),
                    orientation = OrientationDto(0.0, 0.0, 0.0, 1.0),
                    size_mm = 75.0
                )
            )
        )
        var submitted: ScanRequest? = null
        compose.setContent {
            CogniboticsArucoMapperTheme {
                ExportScreen(
                    scan = request,
                    savedScanId = null,
                    onSave = { scan ->
                        submitted = scan
                        throw IllegalStateException("Simulated disconnected API")
                    },
                    onSaved = {},
                    onStartMeasurement = {},
                    onBack = {}
                )
            }
        }
        compose.onNodeWithText("Save measurement to database").performClick()
        compose.waitUntil(5_000) { submitted != null }
        compose.runOnIdle {
            org.junit.Assert.assertEquals(request, submitted)
        }
        compose.onNodeWithText("Could not save measurement").assertExists()
    }
}
