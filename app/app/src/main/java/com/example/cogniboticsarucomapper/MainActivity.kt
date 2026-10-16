package com.example.cogniboticsarucomapper

import android.os.Build
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
import com.example.cogniboticsarucomapper.data.calibration.CalibrationMeasurement
import com.example.cogniboticsarucomapper.data.calibration.CalibrationMeasurementStore
import com.example.cogniboticsarucomapper.data.measurement.MeasurementRequestMapper
import com.example.cogniboticsarucomapper.network.ApiClient
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.ui.calibration.CalibrationScreen
import com.example.cogniboticsarucomapper.ui.calibration.CheckerboardCameraScreen
import com.example.cogniboticsarucomapper.ui.camera.CameraFeedScreen
import com.example.cogniboticsarucomapper.ui.export.ExportScreen
import com.example.cogniboticsarucomapper.ui.home.HomeScreen
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementHistoryItem
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementHistoryScreen
import com.example.cogniboticsarucomapper.ui.measurement.MeasurementScreen
import com.example.cogniboticsarucomapper.ui.measurement.toMeasurementHistoryItem
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val SPLASH_MIN_DURATION_MS = 1_500L

private enum class AppScreen {
    Home,
    Calibration,
    CheckerboardCamera,
    Measurement,
    MeasurementHistory,
    Camera,
    Export
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
                // Keep the pending cell name across screen navigation and activity recreation.
                var workspaceName by rememberSaveable {
                    mutableStateOf("")
                }
                var siteName by rememberSaveable {
                    mutableStateOf("")
                }
                // Store the captured API payload, not a mutable camera frame, across rotation.
                var capturedScanJson by rememberSaveable {
                    mutableStateOf<String?>(null)
                }
                var savedScanId by rememberSaveable {
                    mutableStateOf<String?>(null)
                }
                val gson = remember { Gson() }
                val capturedScan = remember(capturedScanJson) {
                    capturedScanJson?.let { gson.fromJson(it, ScanRequest::class.java) }
                }

                val calibrationStore = remember {
                    CalibrationMeasurementStore(applicationContext)
                }
                var calibrationMeasurement by remember {
                    mutableStateOf(calibrationStore.load())
                }

                var measurements by remember {
                    mutableStateOf<List<MeasurementHistoryItem>>(emptyList())
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == AppScreen.MeasurementHistory) {
                        try {
                            measurements =
                                ApiClient.service.getScans().map { scan ->
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
                                onExportClick = {
                                    currentScreen = AppScreen.Export
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
                                siteName = siteName,
                                onSiteNameChange = { siteName = it },
                                workspaceName = workspaceName,
                                onWorkspaceNameChange = { workspaceName = it },
                                onStartMeasurement = { normalizedSite, normalizedCell ->
                                    siteName = normalizedSite
                                    workspaceName = normalizedCell
                                    capturedScanJson = null
                                    savedScanId = null
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
                                workspaceName = workspaceName,
                                onCapture = { frame ->
                                    val request = MeasurementRequestMapper.fromFrame(
                                        frame = frame,
                                        siteName = siteName,
                                        cellName = workspaceName,
                                        deviceId = listOf(Build.MANUFACTURER, Build.MODEL)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" ")
                                            .ifBlank { "Android device" },
                                        measuredAt = utcMeasurementTime()
                                    )
                                    if (request != null) {
                                        capturedScanJson = gson.toJson(request)
                                        savedScanId = null
                                        currentScreen = AppScreen.Export
                                        true
                                    } else {
                                        false
                                    }
                                },
                                onBack = {
                                    currentScreen = AppScreen.Measurement
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.Export -> {
                            ExportScreen(
                                scan = capturedScan,
                                savedScanId = savedScanId,
                                onSave = { scan -> ApiClient.service.createScan(scan) },
                                onSaved = { savedId -> savedScanId = savedId },
                                onStartMeasurement = {
                                    currentScreen = AppScreen.Measurement
                                },
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

private fun utcMeasurementTime(): String = SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    Locale.US
).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date())
