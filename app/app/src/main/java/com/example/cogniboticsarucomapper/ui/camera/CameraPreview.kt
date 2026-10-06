package com.example.cogniboticsarucomapper.ui.camera

import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cogniboticsarucomapper.data.VisionEngine
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    visionEngine: VisionEngine,
    torchEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fps by visionEngine.fps.collectAsStateWithLifecycle()

    val cameraController = remember {
        val controller = LifecycleCameraController(context)
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        controller.setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), visionEngine)
        controller
    }

    DisposableEffect(lifecycleOwner, cameraController) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }

    LaunchedEffect(cameraController, torchEnabled) {
        cameraController.enableTorch(torchEnabled)
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                val previewView = PreviewView(viewContext)
                previewView.controller = cameraController
                previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                previewView
            },
        )

        Text(
            text = "${"%.0f".format(fps)} fps",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )
    }
}
