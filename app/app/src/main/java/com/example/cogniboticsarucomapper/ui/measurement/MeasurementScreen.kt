package com.example.cogniboticsarucomapper.ui.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MeasurementScreen(
    onStartMeasurement: () -> Unit,
    onHistoryClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Measurement")

        Button(onClick = onStartMeasurement) {
            Text(text = "Start Measurement")
        }

        Button(onClick = onHistoryClick) {
            Text(text = "Measurement History")
        }

        Button(onClick = onBack) {
            Text(text = "Back")
        }
    }
}