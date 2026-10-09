package com.example.cogniboticsarucomapper.ui.calibration

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel

@Composable
fun CalibrationScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    FeatureLayout("Calibration", "Camera calibration workspace", onBack, modifier) {
        InfoPanel("Calibration tools", "Calibration controls will appear here when available.")
    }
}
