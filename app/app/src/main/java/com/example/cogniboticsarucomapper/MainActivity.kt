package com.example.cogniboticsarucomapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cogniboticsarucomapper.ui.calibration.CalibrationScreen
import com.example.cogniboticsarucomapper.ui.camera.CameraFeedScreen
import com.example.cogniboticsarucomapper.ui.export.ExportScreen
import com.example.cogniboticsarucomapper.ui.home.HomeScreen
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementScreen
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementHistoryScreen

private enum class AppScreen {
    Home,
    Calibration,
    Measurement,
    MeasurementHistory,
    Camera,
    Export
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CogniboticsArucoMapperTheme {

                var currentScreen by remember {
                    mutableStateOf(AppScreen.Home)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (currentScreen) {

                        AppScreen.Home -> {
                            HomeScreen(
                                onCalibrationClick = {
                                    currentScreen = AppScreen.Calibration
                                },
                                onMeasurementClick = {
                                    currentScreen = AppScreen.Measurement
                                },
                                onExportClick = {
                                    currentScreen = AppScreen.Export
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Calibration -> {
                            CalibrationScreen(
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Measurement -> {
                            MeasurementScreen(
                                onStartMeasurement = {
                                    currentScreen = AppScreen.Camera
                                },
                                onHistoryClick = {
                                    currentScreen = AppScreen.MeasurementHistory
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.MeasurementHistory -> {
                            MeasurementHistoryScreen(
                                measurements = emptyList(),
                                onBack = {
                                    currentScreen = AppScreen.Measurement
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Camera -> {
                            CameraFeedScreen(
                                onBack = {
                                    currentScreen = AppScreen.Measurement
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Export -> {
                            ExportScreen(
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}