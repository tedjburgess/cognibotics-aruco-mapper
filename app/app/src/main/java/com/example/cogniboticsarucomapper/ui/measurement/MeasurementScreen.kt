package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout

@Composable
fun MeasurementScreen(onStartMeasurement: () -> Unit, onHistoryClick: () -> Unit,
    onBack: () -> Unit, modifier: Modifier = Modifier) {
    FeatureLayout("Measurement", "Scan markers or review previous measurements", onBack, modifier) {
        ActionCard("01", "Start Measurement", "Open the live camera feed", onStartMeasurement)
        ActionCard("02", "Measurement History", "View saved measurements", onHistoryClick)
    }
}
