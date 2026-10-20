package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.domain.measurement.toApiDictionary
import com.example.cogniboticsarucomapper.network.ApiClient
import com.example.cogniboticsarucomapper.ui.components.PageHeading
import com.example.cogniboticsarucomapper.vision.VisionEvent
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConfirmationItem(val label: String, val value: String)

@Composable
fun SaveConfirmationScreen(
    siteName: String,
    cellName: String,
    capturedFrame: VisionEvent.FrameMeasured?,
    capturedMarkers: List<VisionEvent.MarkerDetected>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

    val confirmationItems = remember {
        buildList {
            add(ConfirmationItem("Site", siteName))
            add(ConfirmationItem("Cell / Workspace", cellName))
            add(ConfirmationItem("Markers Detected", capturedMarkers.size.toString()))
            add(
                ConfirmationItem(
                    "Distances Measured",
                    capturedFrame?.distances?.size?.toString() ?: "0"
                )
            )
            if (capturedFrame?.distances?.isNotEmpty() == true) {
                capturedFrame?.distances?.forEach { dist ->
                    add(
                        ConfirmationItem(
                            "${dist.from.label} → ${dist.to.label}",
                            dist.distanceMm?.let {
                                String.format(Locale.getDefault(), "%.3f mm", it)
                            }
                                ?: "N/A"
                        )
                    )
                }
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun doSaveMeasurement() {
        saveError = null
        saving = true
        coroutineScope.launch {
            try {
                val frame =
                    capturedFrame ?: throw IllegalStateException("No measurement data available")
                val markers = capturedMarkers

                if (markers.size < 2) throw IllegalStateException("At least 2 markers required")

                val positionedMarkers = markers.filter {
                    it.positionMm != null &&
                        it.positionMm!!.isValid
                }
                if (positionedMarkers.size <
                    2
                ) {
                    throw IllegalStateException(
                        "At least 2 markers with valid 3D positions required"
                    )
                }

                val observations = positionedMarkers.map { marker ->
                    val pos = marker.positionMm!!
                    com.example.cogniboticsarucomapper.network.model.ObservationRequest(
                        aruco_id = marker.markerId,
                        dictionary = marker.family.toApiDictionary(),
                        name = marker.key.label,
                        position = com.example.cogniboticsarucomapper.network.model.PositionDto(
                            pos.x,
                            pos.y,
                            pos.z
                        ),
                        orientation =
                        com.example.cogniboticsarucomapper.network.model.OrientationDto(
                            0.0,
                            0.0,
                            0.0,
                            1.0
                        ),
                        size_mm = marker.family.sizeMm
                    )
                }

                val relations = frame.distances.filter {
                    it.distanceMm.isFinite() &&
                        it.distanceMm >= 0.0
                }.map { dist ->
                    com.example.cogniboticsarucomapper.network.model.RelationRequest(
                        from_aruco_id = dist.from.id,
                        from_dictionary = dist.from.family.toApiDictionary(),
                        to_aruco_id = dist.to.id,
                        to_dictionary = dist.to.family.toApiDictionary(),
                        distance_mm = dist.distanceMm,
                        relative_position = null
                    )
                }

                val deviceId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: "android-device"
                val measuredAt = Instant.now().toString()

                val request = com.example.cogniboticsarucomapper.network.model.ScanRequest(
                    site_name = siteName,
                    cell_name = cellName,
                    device_id = deviceId,
                    measured_at = measuredAt,
                    observations = observations,
                    relations = relations
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.getService().createScan(request)
                }
                android.util.Log.d("SaveConfirmationScreen", "Measurement saved: ${response.id}")

                saveSuccess = true
                onConfirm()
            } catch (e: Exception) {
                android.util.Log.e("SaveConfirmationScreen", "Failed to save measurement", e)
                saveError = "Failed to save: ${e.message}"
            } finally {
                saving = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        PageHeading("Confirm Save", "Review measurement details before saving") {
            if (!saving) onCancel()
        }

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Measurement Summary", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(confirmationItems) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.label, style = MaterialTheme.typography.bodyMedium)
                            Text(item.value, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (saveError != null) {
            Text(
                text = "Error: $saveError",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (saveSuccess) {
            Text(
                text = "Saved successfully!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onCancel() },
                enabled = !saving && !saveSuccess,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { if (!saving && !saveSuccess) doSaveMeasurement() },
                enabled = !saving && !saveSuccess,
                modifier = Modifier.weight(1f)
            ) {
                if (saving) {
                    Text("Saving...")
                } else if (saveSuccess) {
                    Text("Saved")
                } else {
                    Text("Save to Database")
                }
            }
        }
    }
}
