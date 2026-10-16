package com.example.cogniboticsarucomapper.ui.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.network.model.ObservationRequest
import com.example.cogniboticsarucomapper.network.model.OrientationDto
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
                    state = ExportUiState(),
                    serverAddress = "http://10.0.2.2:8000/",
                    onServerAddressChange = {},
                    onSave = {},
                    onStartMeasurement = {},
                    onBack = {}
                )
            }
        }
        compose.onNodeWithText("No measurement captured").assertExists()
        compose.onNodeWithText("Save measurement to database").assertDoesNotExist()
    }

    @Test
    fun leavingAndReturningDuringSaveKeepsExactlyOneRequestAndOneConfirmation() {
        val started = AtomicInteger(0)
        val submittedIds = mutableListOf<String>()
        val complete = CompletableDeferred<ScanResponse>()
        val request = exampleScan()
        val vm = ExportViewModel { id, payload ->
            started.incrementAndGet()
            submittedIds.add(id)
            assertEquals(request, payload)
            complete.await()
        }
        var showExport by mutableStateOf(true)
        compose.runOnUiThread { assertTrue(vm.capture(request)) }
        compose.setContent {
            CogniboticsArucoMapperTheme {
                if (showExport) {
                    ExportScreen(
                        state = vm.uiState,
                        serverAddress = "http://10.0.2.2:8000/",
                        onServerAddressChange = {},
                        onSave = vm::save,
                        onStartMeasurement = {},
                        onBack = { showExport = false }
                    )
                }
            }
        }

        compose.onNodeWithText("Save measurement to database").performClick()
        compose.waitUntil(5_000) { started.get() == 1 }
        compose.onNodeWithText("Saving...").assertExists()
        compose.runOnIdle {
            assertFalse(vm.startNewMeasurement())
            vm.save()
            showExport = false
        }
        compose.runOnIdle { showExport = true }
        compose.onNodeWithText("Saving...").assertIsNotEnabled()
        compose.runOnIdle { complete.complete(savedScan()) }
        compose.waitUntil(5_000) { vm.uiState.savedScanId == "saved-scan" }
        compose.onNodeWithText("Measurement saved successfully").assertExists()
        compose.runOnIdle {
            vm.save()
            assertEquals(1, started.get())
            assertEquals(1, submittedIds.distinct().size)
            assertFalse(vm.uiState.isSaving)
        }
    }

    @Test
    fun retryAfterUncertainResponseReusesSameIdempotencyKey() {
        val ids = mutableListOf<String>()
        val vm = ExportViewModel { id, _ ->
            ids.add(id)
            if (ids.size == 1) error("Response lost")
            savedScan()
        }
        compose.runOnUiThread { vm.capture(exampleScan()) }
        compose.setContent {
            CogniboticsArucoMapperTheme {
                ExportScreen(
                    state = vm.uiState,
                    serverAddress = "http://10.0.2.2:8000/",
                    onServerAddressChange = {},
                    onSave = vm::save,
                    onStartMeasurement = {},
                    onBack = {}
                )
            }
        }

        compose.onNodeWithText("Save measurement to database").performClick()
        compose.waitUntil(5_000) { vm.uiState.errorMessage != null }
        compose.onNodeWithText("Could not save measurement").assertExists()
        compose.onNodeWithText("Save measurement to database").performClick()
        compose.waitUntil(5_000) { vm.uiState.savedScanId != null }
        compose.runOnIdle {
            assertEquals(2, ids.size)
            assertEquals(ids.first(), ids.last())
        }
    }

    private fun exampleScan() = ScanRequest(
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

    private fun savedScan() = ScanResponse(
        id = "saved-scan",
        site_name = "Factory",
        cell_name = "Cell A",
        device_id = "Android",
        measured_at = "2026-10-16T01:00:00Z",
        created_at = "2026-10-16T01:00:01Z",
        observations = emptyList(),
        relations = emptyList()
    )
}
