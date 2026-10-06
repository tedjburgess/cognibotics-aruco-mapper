package com.example.cogniboticsarucomapper.ui.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.domain.FrameMeasurement
import com.example.cogniboticsarucomapper.domain.ReferenceMarker
import com.example.cogniboticsarucomapper.data.getDictionaryLabelByCode

@Composable
fun ColumnScope.ScanResults(
    reference: ReferenceMarker?,
    measurement: FrameMeasurement?,
    errorMessage: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        val message = singleMessage(reference, measurement, errorMessage)

        if (message != null) {
            var messageColor = MaterialTheme.colorScheme.onSurface
            if (errorMessage != null) {
                messageColor = MaterialTheme.colorScheme.error
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = messageColor,
            )
        } else if (measurement != null && reference != null) {
            Text(
                text = "Sizes",
                style = MaterialTheme.typography.titleSmall,
            )

            for (size in measurement.sizes) {
                var suffix = ""
                val isReference = size.dictionary == reference.dictionary &&
                    size.markerId == reference.markerId
                if (isReference) {
                    suffix = "  (reference)"
                }

                Text(
                    text = "${getDictionaryLabelByCode(size.dictionary)} #${size.markerId}  ${"%.1f".format(size.sizeMm)} mm  ${"%.0f".format(size.edgeLengthPx)} px$suffix",

                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = "Distances",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp),
            )

            if (measurement.distances.isEmpty()) {
                Text(
                    text = "Only one marker in frame.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                for (pair in measurement.distances) {
                    Text(
                        text = "${getDictionaryLabelByCode(pair.from.dictionary)} #${pair.from.markerId} -> ${getDictionaryLabelByCode(pair.to.dictionary)} #${pair.to.markerId}  ${"%.1f".format(pair.distanceMm)} mm",

                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun singleMessage(
    reference: ReferenceMarker?,
    measurement: FrameMeasurement?,
    errorMessage: String?,
): String? {
    if (errorMessage != null) {
        return errorMessage
    }

    if (reference == null) {
        return "No reference marker configured. Set one on the Calibration screen."
    }

    if (measurement == null || measurement.detectedCount == 0) {
        return "No markers detected. Trying ${getDictionaryLabelByCode(reference.dictionary)} only. " +
            "The markers may need more light, more pixels, or a white margin."
    }

    if (!measurement.referenceVisible) {
        return "Reference marker #${reference.markerId} (${getDictionaryLabelByCode(reference.dictionary)}) " +
            "is not in frame. Sizes and distances need it."
    }

    return null
}
