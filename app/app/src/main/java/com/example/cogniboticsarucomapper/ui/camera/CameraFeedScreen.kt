package com.example.cogniboticsarucomapper.ui.camera

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cogniboticsarucomapper.data.ArucoAnalyzer
import com.example.cogniboticsarucomapper.data.OpenCv
import com.example.cogniboticsarucomapper.data.VisionEngine
import com.example.cogniboticsarucomapper.data.readSensorGeometry
import com.example.cogniboticsarucomapper.domain.FrameMeasurement
import com.example.cogniboticsarucomapper.domain.ReferenceMarkerState
import com.example.cogniboticsarucomapper.domain.VisionEvent
import com.example.cogniboticsarucomapper.ui.common.PermissionPrompt
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFeedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val opencvReady = remember { OpenCv.ensureInitialized() }
    val sensorGeometry = remember { readSensorGeometry(context) }
    val reference = ReferenceMarkerState.value

    val analyzer = remember {
        ArucoAnalyzer(
            sensorGeometry = sensorGeometry,
            reference = reference,
        )
    }
    val visionEngine = remember { VisionEngine(analyzers = listOf(analyzer)) }

    var measurement by remember { mutableStateOf<FrameMeasurement?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    val analysisEnabled by visionEngine.analysisEnabled.collectAsStateWithLifecycle()

    val opencvStatus: String
    if (opencvReady) {
        opencvStatus = "OK"
    } else {
        opencvStatus = "FAIL"
    }

    LaunchedEffect(visionEngine) {
        visionEngine.events.collect { event ->
            when (event) {
                is VisionEvent.MeasurementUpdated -> {
                    measurement = event.measurement
                    errorMessage = null
                }
                is VisionEvent.MarkersCleared -> {
                    measurement = null
                }
                is VisionEvent.AnalysisError -> {
                    errorMessage = event.message
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            permissionState.status.isGranted -> {
                CameraPreview(
                    visionEngine = visionEngine,
                    torchEnabled = torchEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.4f),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Switch(
                            checked = analysisEnabled,
                            onCheckedChange = visionEngine::setAnalysisEnabled,
                        )
                        Text(
                            text = "Analysis",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Switch(
                            checked = torchEnabled,
                            onCheckedChange = { enabled -> torchEnabled = enabled },
                        )
                        Text(
                            text = "Torch",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    Text(
                        text = "${measurement?.detectedCount ?: 0} marker(s)",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                    )

                    Badge {
                        Text(text = "OPENCV $opencvStatus")
                    }
                }

                ScanResults(
                    reference = reference,
                    measurement = measurement,
                    errorMessage = errorMessage,
                )
            }

            permissionState.status.shouldShowRationale -> {
                PermissionPrompt(
                    message = "Camera access is needed to show the live feed.",
                    onRequest = { permissionState.launchPermissionRequest() },
                )
            }

            else -> {
                PermissionPrompt(
                    message = "Grant camera permission to continue.",
                    onRequest = { permissionState.launchPermissionRequest() },
                )
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Text(text = "Back")
        }
    }
}
