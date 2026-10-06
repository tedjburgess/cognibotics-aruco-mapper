package com.example.cogniboticsarucomapper.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.cogniboticsarucomapper.data.SUPPORTED_DICTIONARIES
import com.example.cogniboticsarucomapper.data.DEFAULT_DICTIONARY
import com.example.cogniboticsarucomapper.data.dictionaryLabel
import com.example.cogniboticsarucomapper.domain.ReferenceMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = ReferenceMarkerState.value

    var dictionary by remember { mutableStateOf(saved?.dictionary ?: DEFAULT_DICTIONARY) }
    var markerIdText by remember { mutableStateOf(saved?.markerId?.toString() ?: "0") }
    var sizeText by remember { mutableStateOf(saved?.sizeMm?.toString() ?: "100.0") }
    var expanded by remember { mutableStateOf(false) }

    val markerId = markerIdText.toIntOrNull()
    val sizeMm = sizeText.toDoubleOrNull()
    val canSave = markerId != null && sizeMm != null && sizeMm > 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { isExpanded -> expanded = isExpanded },
        ) {
            OutlinedTextField(
                value = dictionaryLabel(dictionary),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Dictionary") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                for (option in SUPPORTED_DICTIONARIES) {
                    DropdownMenuItem(
                        text = { Text(text = option.label) },
                        onClick = {
                            dictionary = option.code
                            expanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = markerIdText,
            onValueChange = { input ->
                markerIdText = input.filter { character -> character.isDigit() }.take(4)
            },
            label = { Text(text = "Marker ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = sizeText,
            onValueChange = { input ->
                sizeText = input.filter { character -> character.isDigit() || character == '.' }
            },
            label = { Text(text = "Size (mm)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                ReferenceMarkerState.save(dictionary, markerId, sizeMm)
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Save")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back")
        }
    }
}
