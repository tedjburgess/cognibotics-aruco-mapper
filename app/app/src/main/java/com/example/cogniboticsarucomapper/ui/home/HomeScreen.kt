package com.example.cogniboticsarucomapper.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cogniboticsarucomapper.R
import com.example.cogniboticsarucomapper.ui.components.ActionCard
import com.example.cogniboticsarucomapper.ui.theme.CogniNavy
import com.example.cogniboticsarucomapper.ui.theme.CogniTealLight
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme

@Composable
fun HomeScreen(onCalibrationClick: () -> Unit, onMeasurementClick: () -> Unit,
    onExportClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("COGNIBOTICS", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Surface(color = CogniNavy, contentColor = Color.White, shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ArUco Mapper", style = MaterialTheme.typography.headlineLarge)
                Text("Calibration · Measurement · Export", style = MaterialTheme.typography.bodyMedium,
                    color = CogniTealLight)
            }
        }
        Text("Workspace", modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.titleLarge)
        ActionCard("01", "Calibration", "Open the calibration workspace", onCalibrationClick)
        ActionCard("02", "Measurement", "Start a scan or view measurement history", onMeasurementClick)
        ActionCard("03", "Export", "Open the export workspace", onExportClick)
        Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Project team", style = MaterialTheme.typography.titleMedium)
            Text("Adam Pisula\nMhd Osama Alsaheb\nOsayi Uwadiae\nRalph Tolentino Ariza\nSimon Ostini\nTed J. Burgess",
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                Image(painterResource(R.drawable.hkr_logo), contentDescription = "HKR logo",
                    modifier = Modifier.padding(8.dp).width(120.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CogniboticsArucoMapperTheme {
        HomeScreen(onCalibrationClick = {}, onMeasurementClick = {}, onExportClick = {})
    }
}
