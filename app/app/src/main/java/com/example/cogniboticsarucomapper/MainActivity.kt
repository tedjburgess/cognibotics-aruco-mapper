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
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import com.example.cogniboticsarucomapper.network.ApiClient
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementHistoryItem
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

                var measurements by remember {
                    mutableStateOf<List<MeasurementHistoryItem>>(emptyList())
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.MeasurementHistory) {
                        try {
                            measurements = ApiClient.service.getScans().map { scan ->
                                MeasurementHistoryItem(
                                    id = scan.id,
                                    siteName = scan.site_name,
                                    cellName = scan.cell_name,
                                    measuredAt = scan.measured_at,
                                    observationCount = scan.observations.size
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "MainActivity",
                                "Failed to load measurement history",
                                e
                            )
                        }
                    }
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
                                measurements = measurements,
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