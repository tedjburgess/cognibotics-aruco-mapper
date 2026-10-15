package com.example.cogniboticsarucomapper.ui.calibration

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cogniboticsarucomapper.domain.calibration.CheckerboardConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckerboardCameraScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permission = rememberPermissionState(Manifest.permission.CAMERA)
    var askedPermission by rememberSaveable { mutableStateOf(false) }
    var columns by rememberSaveable { mutableStateOf("") }
    var rows by rememberSaveable { mutableStateOf("") }
    var squareSize by rememberSaveable { mutableStateOf("") }
    var photos by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    val sessionId = rememberSaveable { UUID.randomUUID().toString() }
    var torch by rememberSaveable { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<PreviewView?>(null) }
    val active = remember { AtomicBoolean(true) }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val board = CheckerboardConfig.parse(columns, rows, squareSize)
    val panelHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.45f).coerceAtMost(360.dp)
    val controller = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            isPinchToZoomEnabled = false
        }
    }

    // CameraX owns the camera lifecycle; release the binding when this screen is removed.
    DisposableEffect(controller) {
        active.set(true)
        onDispose {
            active.set(false)
            controller.unbind()
        }
    }
    DisposableEffect(preview, lifecycleOwner) {
        val view = preview
        val observer = Observer<PreviewView.StreamState> {
            ready =
                it == PreviewView.StreamState.STREAMING
        }
        view?.previewStreamState?.observe(lifecycleOwner, observer)
        onDispose { view?.previewStreamState?.removeObserver(observer) }
    }
    LaunchedEffect(controller, ready, torch) {
        if (ready && controller.cameraInfo?.hasFlashUnit() == true) {
            controller.enableTorch(torch)
        }
    }

    fun leave() {
        if (!capturing) {
            if (photos.isEmpty()) onBack() else confirmation = "leave"
        }
    }

    // Only this session's private cache files are removed, never gallery photos or saved calibration.
    fun clearPhotos() {
        photos.forEach { File(it).delete() }
        photos = arrayListOf()
        message = null
    }
    BackHandler { leave() }

    Column(modifier.fillMaxSize()) {
        Text(
            "Checkerboard camera",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (permission.status.isGranted) {
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                        this.controller = controller
                        preview = this
                        runCatching { controller.bindToLifecycle(lifecycleOwner) }
                            .onFailure {
                                message = "Camera could not start. Go back and try again."
                            }
                    }
                })
            } else {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Allow camera access to capture your checkerboard.")
                    val requestable = !askedPermission || permission.status.shouldShowRationale
                    Button(onClick = {
                        if (requestable) {
                            askedPermission = true
                            permission.launchPermissionRequest()
                        } else {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }) { Text(if (requestable) "Allow camera" else "Open settings") }
                }
            }
        }
        Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp)) {
            Column(
                Modifier.heightIn(
                    max = panelHeight
                ).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Board setup", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Count inner corners, not squares. " +
                        "A 10 × 7 square board has 9 × 6 inner corners.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoardField(
                        "Columns",
                        columns,
                        { columns = it },
                        Modifier.weight(1f),
                        photos.isEmpty() && !capturing,
                        KeyboardType.Number
                    )
                    BoardField(
                        "Rows",
                        rows,
                        { rows = it },
                        Modifier.weight(1f),
                        photos.isEmpty() && !capturing,
                        KeyboardType.Number
                    )
                }
                BoardField(
                    "One square side (mm)",
                    squareSize,
                    {
                        squareSize = it
                    },
                    Modifier.fillMaxWidth(),
                    photos.isEmpty() && !capturing,
                    KeyboardType.Decimal
                )
                if (board == null) {
                    Text(
                        "Enter 2–30 inner corners in each direction and a positive square size.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Keep the whole board visible and still. Capture different positions and tilts.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Torch", Modifier.weight(1f))
                    Switch(
                        checked = torch,
                        onCheckedChange = { torch = it },
                        enabled =
                        ready && !capturing && controller.cameraInfo?.hasFlashUnit() == true
                    )
                }
                Text(
                    "${photos.size}/20 photos captured",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    message ?: if (permission.status.isGranted && !ready) {
                        "Starting camera…"
                    } else {
                        "Photos are temporary. No calibration has been calculated or saved yet."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = ready && board != null && !capturing && photos.size < 20,
                        onClick = {
                            // Keep original CameraX JPEGs for the detection/calibration step in #13.
                            val file = runCatching {
                                val directory = File(context.cacheDir, "checkerboard/$sessionId")
                                check(directory.isDirectory || directory.mkdirs())
                                File.createTempFile("board-", ".jpg", directory)
                            }.getOrElse {
                                message = "Could not create a photo. Check available storage."
                                return@Button
                            }
                            capturing = true
                            message = null
                            val callback = object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    if (!active.get()) {
                                        file.delete()
                                        return
                                    }
                                    photos = ArrayList(photos + file.absolutePath)
                                    capturing = false
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    file.delete()
                                    if (active.get()) {
                                        capturing = false
                                        message =
                                            "Photo failed. Hold the phone still and try again."
                                    }
                                }
                            }
                            runCatching {
                                controller.takePicture(
                                    ImageCapture.OutputFileOptions.Builder(file).build(),
                                    executor,
                                    callback
                                )
                            }.onFailure {
                                file.delete()
                                capturing = false
                                message = "Photo failed. Wait for the camera and try again."
                            }
                        }
                    ) { Text(if (capturing) "Capturing…" else "Capture photo") }
                    OutlinedButton(
                        enabled = photos.isNotEmpty() && !capturing,
                        onClick = { confirmation = "clear" }
                    ) { Text("Clear") }
                }
                if (photos.isNotEmpty()) {
                    Text(
                        "Clear photos before changing the board setup.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { leave() },
            enabled = !capturing,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text("Back")
        }
    }
    if (confirmation != null) {
        AlertDialog(
            onDismissRequest = {
                confirmation = null
            },
            title = { Text("Discard captured photos?") },
            text = {
                Text(
                    "These temporary photos will be removed. " +
                        "Saved calibration values are not changed."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val leaving = confirmation == "leave"
                    clearPhotos()
                    confirmation = null
                    if (leaving) onBack()
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BoardField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    keyboard: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier
    )
}
