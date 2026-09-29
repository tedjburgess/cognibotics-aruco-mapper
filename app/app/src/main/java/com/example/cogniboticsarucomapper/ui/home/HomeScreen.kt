package com.example.cogniboticsarucomapper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme

@Composable
fun HomeScreen(
    onCalibrationClick: () -> Unit,
    onMeasurementClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Cognibotics ArUco Mapper",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onCalibrationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Calibration")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = onMeasurementClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Measurement")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Export")
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        val authors = listOf(
            "Adam Pisula",
            "Mhd Osama Alsaheb",
            "Osayi Uwadiae",
            "Ralph Tolentino Ariza",
            "Simon Ostini",
            "Ted J. Burgess"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Authors",
                style = MaterialTheme.typography.labelMedium
            )

            authors.forEach { author ->
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CogniboticsArucoMapperTheme {
        HomeScreen(
            onCalibrationClick = {},
            onMeasurementClick = {},
            onExportClick = {}
        )
    }
}