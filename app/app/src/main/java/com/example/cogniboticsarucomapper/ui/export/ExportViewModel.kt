package com.example.cogniboticsarucomapper.ui.export

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cogniboticsarucomapper.network.ApiClient
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class ExportUiState(
    val scan: ScanRequest? = null,
    val requestId: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedScanId: String? = null
)

/**
 * Activity-scoped: one save operation remains active across rotation and navigation.
 * A captured scan retains its idempotency key across retries after uncertain responses.
 */
class ExportViewModel(
    private val submitScan: suspend (String, ScanRequest) -> ScanResponse =
        { requestId, scan -> ApiClient.service.createScan(requestId, scan) }
) : ViewModel() {
    var uiState by mutableStateOf(ExportUiState())
        private set

    fun startNewMeasurement(): Boolean {
        if (uiState.isSaving) return false
        uiState = ExportUiState()
        return true
    }

    fun capture(scan: ScanRequest): Boolean {
        if (uiState.isSaving || scan.observations.isEmpty()) return false
        uiState = ExportUiState(
            scan = scan,
            requestId = UUID.randomUUID().toString()
        )
        return true
    }

    fun save() {
        val current = uiState
        val scan = current.scan ?: return
        val requestId = current.requestId ?: return
        if (current.isSaving || current.savedScanId != null || scan.observations.isEmpty()) return

        // Set this synchronously before launching so a second click cannot race the first.
        uiState = current.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = submitScan(requestId, scan)
                uiState = uiState.copy(savedScanId = response.id, errorMessage = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("ExportViewModel", "Saving scan failed", error)
                uiState = uiState.copy(
                    errorMessage = "Could not contact or confirm the API. " +
                        "Check the server address and connection, then retry. " +
                        "The same request ID will be reused."
                )
            } finally {
                uiState = uiState.copy(isSaving = false)
            }
        }
    }
}
