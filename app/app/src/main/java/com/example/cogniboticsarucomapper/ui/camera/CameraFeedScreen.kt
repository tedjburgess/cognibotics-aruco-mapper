package com.example.cogniboticsarucomapper.ui.camera

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cogniboticsarucomapper.vision.OpenCv
import com.example.cogniboticsarucomapper.vision.VisionEngine
import com.example.cogniboticsarucomapper.vision.VisionEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors

private const val TAG = "CameraFeedScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFeedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val opencvReady = remember { OpenCv.ensureInitialized() }
    val visionEngine = remember { VisionEngine() }
    val recentEvents = remember { mutableStateListOf<VisionEvent.MarkerDetected>() }
    val analysisEnabled by visionEngine.analysisEnabled.collectAsStateWithLifecycle()
    val fps by visionEngine.fps.collectAsStateWithLifecycle()
    var torchEnabled by remember { mutableStateOf(false) }

    // React to vision events here. Anything downstream (UI, logging, the
    // future ArUco mapping logic) hooks into the same flow.
    LaunchedEffect(visionEngine) {
        visionEngine.events.collect { event ->
            when (event) {
                is VisionEvent.MarkerDetected -> {
                    recentEvents.removeAll { it.markerId == event.markerId }
                    recentEvents.add(0, event)
                    while (recentEvents.size > 5) recentEvents.removeAt(recentEvents.lastIndex)
                }
                is VisionEvent.MarkersCleared -> recentEvents.clear()
                is VisionEvent.AnalysisError -> Log.e(TAG, "Vision error: ${event.message}", event.throwable)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            permissionState.status.isGranted -> {
                var controller by remember { mutableStateOf<LifecycleCameraController?>(null) }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val camController = LifecycleCameraController(ctx).apply {
                                setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                setImageAnalysisAnalyzer(
                                    Executors.newSingleThreadExecutor(),
                                    visionEngine
                                )
                                bindToLifecycle(lifecycleOwner)
                            }
                            controller = camController
                            PreviewView(ctx).apply {
                                this.controller = camController
                                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                            }
                        }
                    )
                    Text(
                        text = "%.0f fps".format(fps),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                controller?.enableTorch(torchEnabled)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OptionToggle(
                        label = "Analysis",
                        checked = analysisEnabled,
                        onCheckedChange = visionEngine::setAnalysisEnabled
                    )
                    OptionToggle(
                        label = "Torch",
                        checked = torchEnabled,
                        onCheckedChange = { torchEnabled = it }
                    )
                    Text(
                        text = "${recentEvents.size} marker(s)",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Badge { Text("OPENCV ${if (opencvReady) "OK" else "FAIL"}") }
                }

                if (recentEvents.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        recentEvents.forEach { event ->
                            Text(
                                text = "Marker ${event.markerId} @ (%.0f, %.0f)".format(
                                    event.center.x, event.center.y
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            permissionState.status.shouldShowRationale -> {
                PermissionPrompt(
                    message = "Camera access is needed to show the live feed.",
                    onRequest = { permissionState.launchPermissionRequest() }
                )
            }

            else -> {
                PermissionPrompt(
                    message = "Grant camera permission to continue.",
                    onRequest = { permissionState.launchPermissionRequest() }
                )
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun OptionToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PermissionPrompt(message: String, onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message)
        Button(onClick = onRequest) { Text("Grant Camera Permission") }
    }
}