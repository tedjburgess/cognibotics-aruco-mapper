package com.example.cogniboticsarucomapper.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.data.calibration.CalibrationMeasurement
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.components.FeatureLayout
import com.example.cogniboticsarucomapper.ui.components.InfoPanel
import com.example.cogniboticsarucomapper.util.DateTimeFormats

@Composable
fun CalibrationScreen(
    measurement: CalibrationMeasurement,
    onOpenCamera: () -> Unit,
    onUpdate: (CalibrationMeasurement) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by rememberSaveable(
        measurement,
        stateSaver = calibrationDraftSaver
    ) {
        mutableStateOf(measurement.toDraft())
    }
    var showResetConfirmation by remember { mutableStateOf(false) }
    val originalDraft = measurement.toDraft()
    val hasChanges = draft != originalDraft

    FeatureLayout(
        "Calibration",
        "Review and manage the saved camera calibration",
        onBack,
        modifier
    ) {
        if (measurement.isEmpty()) {
            InfoPanel(
                "No calibration saved",
                "Calibration results will appear here when available. You can also enter values below."
            )
        } else {
            InfoPanel(
                "Saved calibration",
                measurement.updatedAtMillis?.let {
                    "Last updated ${DateTimeFormats.formatEpochMillisForDevice(it)}"
                }
                    ?: "Calibration values are saved on this device."
            )
        }

        ActionCard(
            Icons.Filled.CameraAlt,
            "Checkerboard camera",
            "Set up your board and capture different views",
            onOpenCamera
        )

        Text("Image", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Width (px)",
                value = draft.imageWidth,
                onValueChange = { draft = draft.copy(imageWidth = it) },
                isError = !draft.isValidImageDimension(draft.imageWidth),
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            CalibrationValueField(
                label = "Height (px)",
                value = draft.imageHeight,
                onValueChange = { draft = draft.copy(imageHeight = it) },
                isError = !draft.isValidImageDimension(draft.imageHeight),
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Camera matrix", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Focal length X (fx)",
                value = draft.focalLengthX,
                onValueChange = { draft = draft.copy(focalLengthX = it) },
                isError = !draft.isValidDouble(draft.focalLengthX, mustBePositive = true),
                modifier = Modifier.weight(1f)
            )
            CalibrationValueField(
                label = "Focal length Y (fy)",
                value = draft.focalLengthY,
                onValueChange = { draft = draft.copy(focalLengthY = it) },
                isError = !draft.isValidDouble(draft.focalLengthY, mustBePositive = true),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Principal point X (cx)",
                value = draft.principalPointX,
                onValueChange = { draft = draft.copy(principalPointX = it) },
                isError = !draft.isValidDouble(draft.principalPointX),
                modifier = Modifier.weight(1f)
            )
            CalibrationValueField(
                label = "Principal point Y (cy)",
                value = draft.principalPointY,
                onValueChange = { draft = draft.copy(principalPointY = it) },
                isError = !draft.isValidDouble(draft.principalPointY),
                modifier = Modifier.weight(1f)
            )
        }

        Text("Distortion coefficients", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Radial k1",
                value = draft.radialDistortionK1,
                onValueChange = { draft = draft.copy(radialDistortionK1 = it) },
                isError = !draft.isValidDouble(draft.radialDistortionK1),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Text
            )
            CalibrationValueField(
                label = "Radial k2",
                value = draft.radialDistortionK2,
                onValueChange = { draft = draft.copy(radialDistortionK2 = it) },
                isError = !draft.isValidDouble(draft.radialDistortionK2),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Text
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Radial k3",
                value = draft.radialDistortionK3,
                onValueChange = { draft = draft.copy(radialDistortionK3 = it) },
                isError = !draft.isValidDouble(draft.radialDistortionK3),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Text
            )
            CalibrationValueField(
                label = "Tangential p1",
                value = draft.tangentialDistortionP1,
                onValueChange = { draft = draft.copy(tangentialDistortionP1 = it) },
                isError = !draft.isValidDouble(draft.tangentialDistortionP1),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Text
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalibrationValueField(
                label = "Tangential p2",
                value = draft.tangentialDistortionP2,
                onValueChange = { draft = draft.copy(tangentialDistortionP2 = it) },
                isError = !draft.isValidDouble(draft.tangentialDistortionP2),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Text
            )
            CalibrationValueField(
                label = "Reprojection error (px)",
                value = draft.reprojectionErrorPx,
                onValueChange = { draft = draft.copy(reprojectionErrorPx = it) },
                isError = !draft.isValidDouble(draft.reprojectionErrorPx, mustBeNonNegative = true),
                modifier = Modifier.weight(1f)
            )
        }

        if (draft.hasAnyValue() && draft.hasInvalidValues()) {
            Text(
                "Enter at least one value, use positive dimensions and focal lengths, and provide valid numeric values.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onUpdate(draft.toMeasurement(System.currentTimeMillis())) },
                enabled = hasChanges && draft.hasAnyValue() && !draft.hasInvalidValues()
            ) {
                Text("Save calibration")
            }
            OutlinedButton(
                onClick = { showResetConfirmation = true },
                enabled = !measurement.isEmpty() || hasChanges
            ) {
                Text("Reset")
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset calibration?") },
            text = { Text("This removes the saved calibration values from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation = false
                        draft = CalibrationDraft()
                        onReset()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CalibrationValueField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

private data class CalibrationDraft(
    val imageWidth: String = "",
    val imageHeight: String = "",
    val focalLengthX: String = "",
    val focalLengthY: String = "",
    val principalPointX: String = "",
    val principalPointY: String = "",
    val radialDistortionK1: String = "",
    val radialDistortionK2: String = "",
    val radialDistortionK3: String = "",
    val tangentialDistortionP1: String = "",
    val tangentialDistortionP2: String = "",
    val reprojectionErrorPx: String = ""
) {
    fun hasInvalidValues(): Boolean = !isValidImageDimension(imageWidth) ||
        !isValidImageDimension(imageHeight) ||
        !isValidDouble(focalLengthX, mustBePositive = true) ||
        !isValidDouble(focalLengthY, mustBePositive = true) ||
        !isValidDouble(principalPointX) ||
        !isValidDouble(principalPointY) ||
        !isValidDouble(radialDistortionK1) ||
        !isValidDouble(radialDistortionK2) ||
        !isValidDouble(radialDistortionK3) ||
        !isValidDouble(tangentialDistortionP1) ||
        !isValidDouble(tangentialDistortionP2) ||
        !isValidDouble(reprojectionErrorPx, mustBeNonNegative = true)

    fun hasAnyValue(): Boolean = listOf(
        imageWidth,
        imageHeight,
        focalLengthX,
        focalLengthY,
        principalPointX,
        principalPointY,
        radialDistortionK1,
        radialDistortionK2,
        radialDistortionK3,
        tangentialDistortionP1,
        tangentialDistortionP2,
        reprojectionErrorPx
    ).any(String::isNotBlank)

    fun isValidImageDimension(value: String): Boolean =
        value.isBlank() || value.toIntOrNull()?.let { it > 0 } == true

    fun isValidDouble(
        value: String,
        mustBePositive: Boolean = false,
        mustBeNonNegative: Boolean = false
    ): Boolean {
        if (value.isBlank()) return true
        val number = value.toDoubleOrNull() ?: return false
        return number.isFinite() &&
            (!mustBePositive || number > 0) &&
            (!mustBeNonNegative || number >= 0)
    }

    fun toMeasurement(updatedAtMillis: Long): CalibrationMeasurement = CalibrationMeasurement(
        imageWidth = imageWidth.toIntOrNull(),
        imageHeight = imageHeight.toIntOrNull(),
        focalLengthX = focalLengthX.toDoubleOrNull(),
        focalLengthY = focalLengthY.toDoubleOrNull(),
        principalPointX = principalPointX.toDoubleOrNull(),
        principalPointY = principalPointY.toDoubleOrNull(),
        radialDistortionK1 = radialDistortionK1.toDoubleOrNull(),
        radialDistortionK2 = radialDistortionK2.toDoubleOrNull(),
        radialDistortionK3 = radialDistortionK3.toDoubleOrNull(),
        tangentialDistortionP1 = tangentialDistortionP1.toDoubleOrNull(),
        tangentialDistortionP2 = tangentialDistortionP2.toDoubleOrNull(),
        reprojectionErrorPx = reprojectionErrorPx.toDoubleOrNull(),
        updatedAtMillis = updatedAtMillis
    )
}

private val calibrationDraftSaver = listSaver<CalibrationDraft, String>(
    save = { draft ->
        listOf(
            draft.imageWidth,
            draft.imageHeight,
            draft.focalLengthX,
            draft.focalLengthY,
            draft.principalPointX,
            draft.principalPointY,
            draft.radialDistortionK1,
            draft.radialDistortionK2,
            draft.radialDistortionK3,
            draft.tangentialDistortionP1,
            draft.tangentialDistortionP2,
            draft.reprojectionErrorPx
        )
    },
    restore = { saved ->
        CalibrationDraft(
            imageWidth = saved[0],
            imageHeight = saved[1],
            focalLengthX = saved[2],
            focalLengthY = saved[3],
            principalPointX = saved[4],
            principalPointY = saved[5],
            radialDistortionK1 = saved[6],
            radialDistortionK2 = saved[7],
            radialDistortionK3 = saved[8],
            tangentialDistortionP1 = saved[9],
            tangentialDistortionP2 = saved[10],
            reprojectionErrorPx = saved[11]
        )
    }
)

private fun CalibrationMeasurement.toDraft() = CalibrationDraft(
    imageWidth = imageWidth?.toString().orEmpty(),
    imageHeight = imageHeight?.toString().orEmpty(),
    focalLengthX = focalLengthX?.toString().orEmpty(),
    focalLengthY = focalLengthY?.toString().orEmpty(),
    principalPointX = principalPointX?.toString().orEmpty(),
    principalPointY = principalPointY?.toString().orEmpty(),
    radialDistortionK1 = radialDistortionK1?.toString().orEmpty(),
    radialDistortionK2 = radialDistortionK2?.toString().orEmpty(),
    radialDistortionK3 = radialDistortionK3?.toString().orEmpty(),
    tangentialDistortionP1 = tangentialDistortionP1?.toString().orEmpty(),
    tangentialDistortionP2 = tangentialDistortionP2?.toString().orEmpty(),
    reprojectionErrorPx = reprojectionErrorPx?.toString().orEmpty()
)

private fun CalibrationMeasurement.isEmpty(): Boolean = imageWidth == null &&
    imageHeight == null &&
    focalLengthX == null &&
    focalLengthY == null &&
    principalPointX == null &&
    principalPointY == null &&
    radialDistortionK1 == null &&
    radialDistortionK2 == null &&
    radialDistortionK3 == null &&
    tangentialDistortionP1 == null &&
    tangentialDistortionP2 == null &&
    reprojectionErrorPx == null
