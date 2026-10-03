package com.example.cogniboticsarucomapper.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CalibrationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chessboardSize by remember { mutableStateOf("") }
    var marker4x4Size by remember { mutableStateOf("") }
    var marker5x5Size by remember { mutableStateOf("") }
    var marker6x6Size by remember { mutableStateOf("") }
    var marker7x7Size by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Calibration",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chessboard settings",
            style = MaterialTheme.typography.titleMedium
        )

        PhysicalSizeField(
            label = "Chessboard physical size",
            value = chessboardSize,
            onValueChange = { chessboardSize = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Marker physical sizes",
            style = MaterialTheme.typography.titleMedium
        )

        PhysicalSizeField(
            label = "4x4 marker size",
            value = marker4x4Size,
            onValueChange = { marker4x4Size = it }
        )

        PhysicalSizeField(
            label = "5x5 marker size",
            value = marker5x5Size,
            onValueChange = { marker5x5Size = it }
        )

        PhysicalSizeField(
            label = "6x6 marker size",
            value = marker6x6Size,
            onValueChange = { marker6x6Size = it }
        )

        PhysicalSizeField(
            label = "7x7 marker size",
            value = marker7x7Size,
            onValueChange = { marker7x7Size = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back")
        }
    }
}

@Composable
private fun PhysicalSizeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (
                newValue.isEmpty() ||
                newValue.matches(Regex("^\\d*\\.?\\d*$"))
            ) {
                onValueChange(newValue)
            }
        },
        label = {
            Text(text = label)
        },
        suffix = {
            Text(text = "mm")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}