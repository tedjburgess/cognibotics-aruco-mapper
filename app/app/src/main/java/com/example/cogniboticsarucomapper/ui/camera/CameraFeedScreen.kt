package com.example.cogniboticsarucomapper.ui.camera

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cogniboticsarucomapper.vision.ArucoAnalyzer
import com.example.cogniboticsarucomapper.vision.OpenCv
import com.example.cogniboticsarucomapper.vision.VisionEngine
import com.example.cogniboticsarucomapper.vision.VisionEvent
import com.example.cogniboticsarucomapper.vision.MarkerScanState
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
    val controlsMaxHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.35f).coerceAtMost(200.dp)
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val opencvReady = remember { OpenCv.ensureInitialized() }
    val visionEngine = remember { VisionEngine(analyzers = listOf(ArucoAnalyzer())) }
    val visibleMarkers = remember { mutableStateListOf<VisionEvent.MarkerDetected>() }
    val analysisEnabled by visionEngine.analysisEnabled.collectAsStateWithLifecycle()
    val fps by visionEngine.fps.collectAsStateWithLifecycle()
    var torchEnabled by remember { mutableStateOf(false) }

    // React to vision events here. Anything downstream (UI, logging, the
    // future ArUco mapping logic) hooks into the same flow.
    LaunchedEffect(visionEngine) {
        visionEngine.events.collect { event ->
            when (event) {
                is VisionEvent.MarkerDetected -> {
                    visibleMarkers.removeAll { it.markerId == event.markerId }
                    visibleMarkers.add(0, event)
                    while (visibleMarkers.size > 10) visibleMarkers.removeAt(visibleMarkers.lastIndex)
                }
                is VisionEvent.MarkersCleared -> visibleMarkers.clear()
                is VisionEvent.AnalysisError -> Log.e(TAG, "Vision error: ${event.message}", event.throwable)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Live measurement", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        when {
            permissionState.status.isGranted -> {
                var controller by remember { mutableStateOf<LifecycleCameraController?>(null) }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp))) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->

                            val camController = LifecycleCameraController(ctx).apply {

                                setEnabledUseCases(
                                    CameraController.IMAGE_ANALYSIS
                                )

                                cameraSelector =
                                    CameraSelector.DEFAULT_BACK_CAMERA

                                setImageAnalysisAnalyzer(
                                    Executors.newSingleThreadExecutor(),
                                    visionEngine
                                )

                                bindToLifecycle(lifecycleOwner)
                            }

                            controller = camController

                            PreviewView(ctx).apply {
                                this.controller = camController

                                implementationMode =
                                    PreviewView.ImplementationMode.PERFORMANCE
                            }
                        }
                    )

                    // ArUco overlay
                    MarkerOverlay(
                        markers = visibleMarkers,
                        modifier = Modifier.fillMaxSize()
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
                        text = "${visibleMarkers.size} marker(s)",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Badge { Text("OPENCV ${if (opencvReady) "OK" else "FAIL"}") }
                }

                if (visibleMarkers.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        visibleMarkers
                            .sortedBy { it.markerId }
                            .forEach { event ->

                                Text(
                                    text = buildString {
                                        append("Marker ${event.markerId}")
                                        append(" - ${(event.certainty * 100).toInt()}%")

                                        append(
                                            when (event.scanState) {
                                                MarkerScanState.DETECTED ->
                                                    " - Detected"

                                                MarkerScanState.CONFIRMING ->
                                                    " - Confirming"

                                                MarkerScanState.STABLE ->
                                                    " - Stable ✓"
                                            }
                                        )
                                    },
                                    color = markerOutlineColor(event.scanState),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                    }
                }
            }

            permissionState.status.shouldShowRationale -> {
                PermissionPrompt(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    message = "Camera access is needed to show the live feed.",
                    onRequest = { permissionState.launchPermissionRequest() }
                )
            }

            else -> {
                PermissionPrompt(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    message = "Grant camera permission to continue.",
                    onRequest = { permissionState.launchPermissionRequest() }
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

private fun markerOutlineColor(state: MarkerScanState): Color {
    return when (state) {
        MarkerScanState.DETECTED -> Color.Gray
        MarkerScanState.CONFIRMING -> Color.Yellow
        MarkerScanState.STABLE -> Color.Green
    }
}

@Composable
private fun OptionToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PermissionPrompt(message: String, onRequest: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message)
        Button(onClick = onRequest) { Text("Grant Camera Permission") }
    }
}

@Composable
private fun MarkerOverlay(
    markers: List<VisionEvent.MarkerDetected>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        markers.forEach { marker ->

            if (marker.corners.size < 4) {
                return@forEach
            }

            val transformedCorners = marker.corners.map { corner ->

                transformPoint(
                    point = corner,
                    frameWidth = marker.frameWidth,
                    frameHeight = marker.frameHeight,
                    rotationDegrees = marker.rotationDegrees,
                    canvasWidth = size.width,
                    canvasHeight = size.height
                )
            }

            val color = markerOutlineColor(marker.scanState)

            val path = Path().apply {

                moveTo(
                    transformedCorners[0].x,
                    transformedCorners[0].y
                )

                lineTo(
                    transformedCorners[1].x,
                    transformedCorners[1].y
                )

                lineTo(
                    transformedCorners[2].x,
                    transformedCorners[2].y
                )

                lineTo(
                    transformedCorners[3].x,
                    transformedCorners[3].y
                )

                close()
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 6f)
            )
        }
    }
}

private fun transformPoint(
    point: Offset,
    frameWidth: Int,
    frameHeight: Int,
    rotationDegrees: Int,
    canvasWidth: Float,
    canvasHeight: Float
): Offset {

    val rotatedPoint: Offset
    val rotatedWidth: Float
    val rotatedHeight: Float

    when (rotationDegrees) {

        90 -> {
            rotatedPoint = Offset(
                x = frameHeight - point.y,
                y = point.x
            )

            rotatedWidth = frameHeight.toFloat()
            rotatedHeight = frameWidth.toFloat()
        }

        180 -> {
            rotatedPoint = Offset(
                x = frameWidth - point.x,
                y = frameHeight - point.y
            )

            rotatedWidth = frameWidth.toFloat()
            rotatedHeight = frameHeight.toFloat()
        }

        270 -> {
            rotatedPoint = Offset(
                x = point.y,
                y = frameWidth - point.x
            )

            rotatedWidth = frameHeight.toFloat()
            rotatedHeight = frameWidth.toFloat()
        }

        else -> {
            rotatedPoint = point

            rotatedWidth = frameWidth.toFloat()
            rotatedHeight = frameHeight.toFloat()
        }
    }

    val scale = maxOf(
        canvasWidth / rotatedWidth,
        canvasHeight / rotatedHeight
    )

    val offsetX =
        (canvasWidth - rotatedWidth * scale) / 2f

    val offsetY =
        (canvasHeight - rotatedHeight * scale) / 2f

    return Offset(
        x = rotatedPoint.x * scale + offsetX,
        y = rotatedPoint.y * scale + offsetY
    )
}