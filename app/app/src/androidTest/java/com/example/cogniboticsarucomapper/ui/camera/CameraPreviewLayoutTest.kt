package com.example.cogniboticsarucomapper.ui.camera

import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cogniboticsarucomapper.MainActivity
import com.example.cogniboticsarucomapper.ui.theme.CogniboticsArucoMapperTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraPreviewLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun savingStateDoesNotResizeTheLiveCamera() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand(
                "pm grant $packageName android.permission.CAMERA"
            )
        ).use { it.readBytes() }
        val saving = mutableStateOf(false)
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                CogniboticsArucoMapperTheme {
                    CameraFeedScreen(
                        siteName = "Test site",
                        cellName = "Test cell",
                        onBack = {},
                        onSave = { _, _ -> },
                        isSaving = saving.value,
                        onSavingChanged = { saving.value = it }
                    )
                }
            }
        }
        compose.waitUntil(15_000) {
            runCatching { compose.onNodeWithTag("liveCameraPreview").assertExists() }.isSuccess
        }
        val preview = compose.onNodeWithTag("liveCameraPreview")
        val initialBounds = preview.fetchSemanticsNode().boundsInRoot
        // Results and save state must not renegotiate the camera's viewport.
        repeat(6) { index ->
            compose.runOnIdle { saving.value = index % 2 == 0 }
            compose.waitForIdle()
            assertEquals(initialBounds, preview.fetchSemanticsNode().boundsInRoot)
        }
    }
}
