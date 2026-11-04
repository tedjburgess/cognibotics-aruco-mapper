package com.cognibotics.arucomapper

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cognibotics.arucomapper.data.calibration.CalibrationMeasurement
import com.cognibotics.arucomapper.data.calibration.CalibrationMeasurementStore
import com.cognibotics.arucomapper.data.settings.ApiSettingsStore
import com.cognibotics.arucomapper.network.ApiClient
import com.cognibotics.arucomapper.ui.calibration.CalibrationScreen
import com.cognibotics.arucomapper.ui.calibration.CheckerboardCameraScreen
import com.cognibotics.arucomapper.ui.camera.CameraFeedScreen
import com.cognibotics.arucomapper.ui.home.HomeScreen
import com.cognibotics.arucomapper.ui.measurement.MeasurementHistoryItem
import com.cognibotics.arucomapper.ui.measurement.MeasurementHistoryScreen
import com.cognibotics.arucomapper.ui.measurement.MeasurementScreen
import com.cognibotics.arucomapper.ui.measurement.SaveConfirmationScreen
import com.cognibotics.arucomapper.ui.measurement.toMeasurementHistoryItem
import com.cognibotics.arucomapper.ui.settings.SettingsScreen
import com.cognibotics.arucomapper.ui.theme.CogniboticsArucoMapperTheme
import com.cognibotics.arucomapper.vision.VisionEvent

private const val SPLASH_MIN_DURATION_MS = 1_500L

private enum class AppScreen {
    Home,
    Settings,
    Calibration,
    CheckerboardCamera,
    Measurement,
    MeasurementHistory,
    Camera,
    SaveConfirmation
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartedAt = SystemClock.uptimeMillis()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // Keep branding visible on launch without delaying activity recreation.
            splashScreen.setKeepOnScreenCondition {
                SystemClock.uptimeMillis() - splashStartedAt < SPLASH_MIN_DURATION_MS
            }
        }

        enableEdgeToEdge()

        setContent {
            CogniboticsArucoMapperTheme {
                var currentScreen by rememberSaveable {
                    mutableStateOf(AppScreen.Home)
                }
                var siteName by rememberSaveable {
                    mutableStateOf("")
                }
                var cellName by rememberSaveable {
                    mutableStateOf("")
                }

                val calibrationStore = remember {
                    CalibrationMeasurementStore(applicationContext)
                }
                var calibrationMeasurement by remember {
                    mutableStateOf(calibrationStore.load())
                }

                val apiSettings = remember { ApiSettingsStore(applicationContext) }
                var apiBaseUrl by rememberSaveable {
                    mutableStateOf(apiSettings.load().also { ApiClient.setBaseUrl(it) })
                }

                var measurements by remember {
                    mutableStateOf<List<MeasurementHistoryItem>>(emptyList())
                }

                var capturedFrame by remember { mutableStateOf<VisionEvent.FrameMeasured?>(null) }
                var capturedMarkers by remember {
                    mutableStateOf<List<VisionEvent.MarkerDetected>>(emptyList())
                }
                var cameraSaving by remember { mutableStateOf(false) }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.MeasurementHistory) {
                        try {
                            measurements =
                                ApiClient.getService().getScans().map { scan ->
                                    scan.toMeasurementHistoryItem()
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
                                onSettingsClick = {
                                    currentScreen = AppScreen.Settings
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Settings -> {
                            SettingsScreen(
                                apiBaseUrl = apiBaseUrl,
                                defaultApiBaseUrl = BuildConfig.API_BASE_URL,
                                onSave = { url ->
                                    apiSettings.save(url)
                                    apiBaseUrl = url
                                    ApiClient.setBaseUrl(url)
                                },
                                onReset = {
                                    apiSettings.reset()
                                    val fallback = apiSettings.load()
                                    apiBaseUrl = fallback
                                    ApiClient.setBaseUrl(fallback)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Calibration -> {
                            CalibrationScreen(
                                onOpenCamera = { currentScreen = AppScreen.CheckerboardCamera },
                                measurement = calibrationMeasurement,
                                onUpdate = { updatedMeasurement ->
                                    calibrationStore.update(updatedMeasurement)
                                    calibrationMeasurement = updatedMeasurement
                                },
                                onReset = {
                                    calibrationStore.reset()
                                    calibrationMeasurement = CalibrationMeasurement()
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.CheckerboardCamera -> {
                            CheckerboardCameraScreen(
                                onBack = { currentScreen = AppScreen.Calibration },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Measurement -> {
                            MeasurementScreen(
                                onStartMeasurement = { site, cell ->
                                    siteName = site
                                    cellName = cell
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
                                onRefresh = {
                                    measurements = ApiClient.getService().getScans()
                                        .map { it.toMeasurementHistoryItem() }
                                },
                                onDelete = { measurementId ->
                                    ApiClient.getService().deleteScan(measurementId)

                                    measurements = measurements.filterNot {
                                        it.id == measurementId
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Camera -> {
                            CameraFeedScreen(
                                siteName = siteName,
                                cellName = cellName,
                                calibrationMeasurement = calibrationMeasurement,
                                onBack = {
                                    currentScreen = AppScreen.Measurement
                                },
                                onSave = { frame, markers ->
                                    capturedFrame = frame
                                    capturedMarkers = markers
                                    currentScreen = AppScreen.SaveConfirmation
                                },
                                isSaving = cameraSaving,
                                onSavingChanged = { cameraSaving = it },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.SaveConfirmation -> {
                            SaveConfirmationScreen(
                                siteName = siteName,
                                cellName = cellName,
                                capturedFrame = capturedFrame,
                                capturedMarkers = capturedMarkers,
                                onConfirm = {
                                    currentScreen = AppScreen.Measurement
                                    capturedFrame = null
                                    capturedMarkers = emptyList()
                                    cameraSaving = false
                                },
                                onCancel = {
                                    currentScreen = AppScreen.Camera
                                    cameraSaving = false
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
