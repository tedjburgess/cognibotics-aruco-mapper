package com.example.cogniboticsarucomapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cogniboticsarucomapper.ui.camera.CameraFeedScreen
import com.example.cogniboticsarucomapper.ui.home.HomeScreen
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CogniboticsArucoMapperTheme {
                var showCameraFeed by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showCameraFeed) {
                        CameraFeedScreen(
                            onBack = { showCameraFeed = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        HomeScreen(
                            onCalibrationClick = {}, // Calibration screen will be added later
                            onMeasurementClick = { showCameraFeed = true },
                            onExportClick = {}, // Export screen will be added later
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}