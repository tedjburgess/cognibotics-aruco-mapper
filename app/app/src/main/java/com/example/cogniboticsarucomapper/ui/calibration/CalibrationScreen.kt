package com.example.cogniboticsarucomapper.ui.calibration

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel

@Composable
fun CalibrationScreen(onBack: () -> Unit, onOpenCamera: () -> Unit, modifier: Modifier = Modifier) {
    FeatureLayout("Calibration", "Camera calibration workspace", onBack, modifier) {
        InfoPanel(
            "Camera calibration",
            "Capture checkerboard photos. Calibration calculation and saved values will be connected separately."
        )
        ActionCard(
            "01",
            "Checkerboard camera",
            "Set up your board and capture different views",
            onOpenCamera
        )
    }
}
