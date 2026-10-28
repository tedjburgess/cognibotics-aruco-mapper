package com.example.cogniboticsarucomapper.ui.camera

import android.Manifest
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cogniboticsarucomapper.data.measurement.sensorGeometryFor
import com.example.cogniboticsarucomapper.domain.measurement.DistanceDisplayTracker
import com.example.cogniboticsarucomapper.domain.measurement.PairReading
import com.example.cogniboticsarucomapper.domain.measurement.MarkerRole
import com.example.cogniboticsarucomapper.domain.measurement.role
import com.example.cogniboticsarucomapper.util.MeasurementFormats
import com.example.cogniboticsarucomapper.vision.ArucoAnalyzer
import com.example.cogniboticsarucomapper.vision.CameraParameterSource
import com.example.cogniboticsarucomapper.vision.MarkerScanState
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
fun CameraFeedScreen(
    siteName: String,
    cellName: String,
    onBack: () -> Unit,
    onSave: (VisionEvent.FrameMeasured?, List<VisionEvent.MarkerDetected>) -> Unit,
    isSaving: Boolean,
    onSavingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsMaxHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.35f).coerceAtMost(
        300.dp
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val opencvReady = remember { OpenCv.ensureInitialized() }
    val markerAnalyzer = remember(opencvReady) { if (opencvReady) ArucoAnalyzer() else null }
    val visionEngine =
        remember(markerAnalyzer) { VisionEngine(analyzers = listOfNotNull(markerAnalyzer)) }
    // Fixed rows and a short median window make results readable without hiding missing readings.
    val displayTracker = remember { DistanceDisplayTracker() }
    var distances by remember { mutableStateOf<List<PairReading>>(emptyList()) }
    var steadyDisplay by remember { mutableStateOf(true) }
    var cameraParameters by remember { mutableStateOf(CameraParameterSource.UNAVAILABLE) }
    var measurementError by remember { mutableStateOf<String?>(null) }
    val visibleMarkers = remember { mutableStateListOf<VisionEvent.MarkerDetected>() }
    val analysisEnabled by visionEngine.analysisEnabled.collectAsStateWithLifecycle()
    val fps by visionEngine.fps.collectAsStateWithLifecycle()
    var torchEnabled by remember { mutableStateOf(false) }

    var latestFrameMeasured by remember { mutableStateOf<VisionEvent.FrameMeasured?>(null) }
    var latestMarkers by remember { mutableStateOf<List<VisionEvent.MarkerDetected>>(emptyList()) }

    // React to vision events here. Anything downstream (UI, logging, the
    // future ArUco mapping logic) hooks into the same flow.
    LaunchedEffect(visionEngine) {
        visionEngine.events.collect { event ->
            if (isSaving) return@collect
            when (event) {
                is VisionEvent.FrameMeasured -> {
                    if (visionEngine.analysisEnabled.value) {
                        visibleMarkers.clear()
                        visibleMarkers.addAll(event.markers.filter { it.certainty >= 0.3f })
                        distances = displayTracker.update(
                            visibleMarkers.map {
                                it.key
                            }.toSet(),
                            event.distances,
                            SystemClock.elapsedRealtime()
                        )
                        cameraParameters = event.cameraParameters
                        measurementError = null
                        latestFrameMeasured = event
                        latestMarkers = event.markers
                    }
                }
                is VisionEvent.MarkerDetected -> {
                    visibleMarkers.removeAll { it.key == event.key }
                    visibleMarkers.add(0, event)
                    while (visibleMarkers.size >
                        10
                    ) {
                        visibleMarkers.removeAt(visibleMarkers.lastIndex)
                    }
                }
                is VisionEvent.MarkersCleared -> {
                    visibleMarkers.clear()
                    distances =
                        displayTracker.update(
                            emptySet(),
                            emptyList(),
                            SystemClock.elapsedRealtime()
                        )
                    latestFrameMeasured = null
                    latestMarkers = emptyList()
                }
                is VisionEvent.AnalysisError -> {
                    visibleMarkers.clear()
                    distances =
                        displayTracker.update(
                            emptySet(),
                            emptyList(),
                            SystemClock.elapsedRealtime()
                        )
                    measurementError = "Measurement unavailable for this frame."
                    Log.e(TAG, "Vision error: ${event.message}", event.throwable)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Live measurement",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Text(
            text = "Site: $siteName  |  Cell: $cellName",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        when {
            permissionState.status.isGranted -> {
                var controller by remember { mutableStateOf<LifecycleCameraController?>(null) }
                val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
                DisposableEffect(lifecycleOwner) {
                    onDispose {
                        controller?.clearImageAnalysisAnalyzer()
                        controller?.unbind()
                        analysisExecutor.shutdownNow()
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().weight(
                        1f
                    ).padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp))
                ) {
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
                                    analysisExecutor,
                                    visionEngine
                                )

                                isPinchToZoomEnabled = false
                                bindToLifecycle(lifecycleOwner)
                            }

                            controller = camController

                            PreviewView(ctx).apply {
                                this.controller = camController
                                camController.initializationFuture.addListener({
                                    camController.cameraInfo?.let { info ->
                                        markerAnalyzer?.sensorGeometry = sensorGeometryFor(info)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

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

                    Surface(
                        color = Color(0xD9102A43),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    ) {
                        Text(
                            text = "%.0f fps".format(fps),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                controller?.enableTorch(torchEnabled)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        Modifier.heightIn(
                            max = controlsMaxHeight
                        ).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${visibleMarkers.size} marker(s)",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Badge { Text("OPENCV ${if (opencvReady) "OK" else "FAIL"}") }
                        }
                        OptionToggle(
                            label = "Analysis",
                            checked = analysisEnabled,
                            onCheckedChange = { enabled ->
                                visionEngine.setAnalysisEnabled(enabled)
                                if (!enabled) {
                                    visibleMarkers.clear()
                                    displayTracker.reset()
                                    distances = emptyList()
                                    latestFrameMeasured = null
                                    latestMarkers = emptyList()
                                }
                            }
                        )
                        OptionToggle(
                            label = "Torch",
                            checked = torchEnabled,
                            onCheckedChange = { torchEnabled = it }
                        )
                        OptionToggle(
                            label = "Steady display (up to 5 frames)",
                            checked = steadyDisplay,
                            onCheckedChange = { steadyDisplay = it }
                        )
                        Text(
                            text = measurementError ?: when {
                                !analysisEnabled -> "Analysis paused."
                                !opencvReady -> "OpenCV could not start."
                                cameraParameters == CameraParameterSource.UNAVAILABLE ->
                                    "Camera parameters unavailable : distances cannot be measured."
                                cameraParameters == CameraParameterSource.ESTIMATED ->
                                    "Estimated camera parameters : accuracy not validated."
                                else -> "Using calibrated camera parameters."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (distances.isNotEmpty()) {
                            Text(
                                "Marker distances",
                                style = MaterialTheme.typography.titleSmall
                            )
                            distances.forEach { pair ->
                                val value = if (steadyDisplay) pair.steadyMm else pair.rawMm
                                val pairLabel = MeasurementFormats.markerPairLabel(
                                    pair.from,
                                    pair.to
                                )
                                val formattedValue = value?.let {
                                    MeasurementFormats.millimeters(it, 1)
                                } ?: "N/A"
                                val fromRole = pair.from.role()
                                val toRole = pair.to.role()

                                val distanceType =
                                    when {
                                        fromRole == MarkerRole.DIRECTIONAL_REFERENCE &&
                                            toRole == MarkerRole.POSITIONAL ->
                                            "Directional-to-centre"

                                        fromRole == MarkerRole.POSITIONAL &&
                                            toRole == MarkerRole.DIRECTIONAL_REFERENCE ->
                                            "Centre-to-directional"

                                        fromRole == MarkerRole.DIRECTIONAL_REFERENCE &&
                                            toRole == MarkerRole.DIRECTIONAL_REFERENCE ->
                                            "Directional-to-directional"

                                        else ->
                                            "Centre-to-centre"
                                    }
                                Text(
                                    "$pairLabel - $distanceType: $formattedValue",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else if (analysisEnabled && visibleMarkers.size < 2) {
                            Text(
                                "Show two different markers together to measure a distance.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (visibleMarkers.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                visibleMarkers
                                    .sortedWith(
                                        compareBy({
                                            it.dictionary.ordinal
                                        }, { it.markerId })
                                    )
                                    .forEach { event ->
                                        Text(
                                            text = buildString {
                                                append(
                                                    "${MeasurementFormats.markerLabel(
                                                        event.dictionary,
                                                        event.markerId
                                                    )} " +
                                                        "(${MeasurementFormats.millimeters(
                                                            event.dictionary.sizeMm,
                                                            0
                                                        )})"
                                                )
                                                append(
                                                    when (event.role) {
                                                        MarkerRole.DIRECTIONAL_REFERENCE -> " - Directional"
                                                        MarkerRole.POSITIONAL -> " - Position"
                                                    }
                                                )
                                                event.tiltDegrees?.let {
                                                    append(" · tilt ≈%.0f°".format(it))
                                                }
                                                if (event.positionMm ==
                                                    null
                                                ) {
                                                    append(" · pose unavailable")
                                                }
                                                append(
                                                    " - tracking ${(event.certainty * 100).toInt()}%"
                                                )
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
                }

                if (distances.isNotEmpty() && visibleMarkers.size >= 2 && !isSaving) {
                    Button(
                        onClick = {
                            onSavingChanged(true)
                            onSave(latestFrameMeasured, latestMarkers)
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Text("Save Measurement")
                    }
                } else if (isSaving) {
                    Text(
                        "Preparing to save...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else if (analysisEnabled && visibleMarkers.size < 2) {
                    Text(
                        "Detect at least 2 markers to enable saving.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
    }
}

private fun markerOutlineColor(state: MarkerScanState): Color = when (state) {
    MarkerScanState.DETECTED -> Color.Gray
    MarkerScanState.CONFIRMING -> Color.Yellow
    MarkerScanState.STABLE -> Color.Green
}

@Composable
private fun OptionToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PermissionPrompt(
    message: String,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
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
