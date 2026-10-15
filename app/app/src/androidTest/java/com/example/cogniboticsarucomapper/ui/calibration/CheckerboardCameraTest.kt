package com.example.cogniboticsarucomapper.ui.calibration

import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cogniboticsarucomapper.MainActivity
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckerboardCameraTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun allowCamera() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        val command = "pm grant $packageName android.permission.CAMERA"
        ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand(command)
        )
            .use { it.readBytes() }
    }

    private fun openAndSetBoard() {
        // The launch splash uses real time; wait for the first composed screen.
        compose.waitUntil(15_000) {
            runCatching { compose.onNodeWithText("Calibration").assertExists() }.isSuccess
        }
        compose.onNodeWithText("Calibration").performClick()
        compose.onNodeWithText("Checkerboard camera").performClick()
        compose.onNodeWithText("Columns").performTextInput("9")
        compose.onNodeWithText("Rows").performTextInput("6")
        compose.onNodeWithText("One square side (mm)").performTextInput("25.5")
        closeSoftKeyboard()
    }

    @Test
    fun setupAndBackNavigationSurviveRecreation() {
        openAndSetBoard()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("9").assertExists()
        compose.onNodeWithText("6").assertExists()
        compose.onNodeWithText("25.5").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Camera calibration workspace").assertExists()
    }

    @Test
    fun capturesRealJpegAndDiscardsOnlyTheSessionPhoto() {
        val root =
            File(
                InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
                "checkerboard"
            )
        val before = root.walkTopDown().filter { it.isFile }.map { it.absolutePath }.toSet()
        openAndSetBoard()
        compose.waitUntil(30_000) {
            runCatching { compose.onNodeWithText("Capture photo").assertIsEnabled() }.isSuccess
        }
        compose.onNodeWithText("Capture photo").performScrollTo().performClick()
        compose.waitUntil(20_000) {
            runCatching { compose.onNodeWithText("1/20 photos captured").assertExists() }.isSuccess
        }
        val image = root.walkTopDown().single { it.isFile && it.absolutePath !in before }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(image.absolutePath, bounds)
        assertTrue(image.length() > 0 && bounds.outWidth > 0 && bounds.outHeight > 0)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("1/20 photos captured").assertExists()
        compose.onNodeWithText("9").assertIsNotEnabled()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("1/20 photos captured").assertExists()
        compose.onNodeWithText("Clear").performScrollTo().performClick()
        compose.onNodeWithText("Discard").performClick()
        compose.onNodeWithText("0/20 photos captured").assertExists()
        compose.onNodeWithText("9").assertIsEnabled()
        assertTrue(!image.exists())
        assertTrue(before.all { File(it).exists() })
    }
}
