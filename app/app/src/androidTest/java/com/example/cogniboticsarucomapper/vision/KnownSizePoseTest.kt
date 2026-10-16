package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.data.measurement.CameraModel
import com.example.cogniboticsarucomapper.data.measurement.MarkerPoseSolver
import com.example.cogniboticsarucomapper.domain.measurement.MarkerFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.objdetect.Objdetect

@RunWith(AndroidJUnit4::class)
class KnownSizePoseTest {
    @Test
    fun detectsSmallerCapacityCodesAcrossFamiliesAndClearsTheNextEmptyFrame() {
        assertTrue(OpenCv.ensureInitialized())
        val board = Mat(600, 600, CvType.CV_8UC1, Scalar(255.0))
        val codes = listOf(
            Objdetect.DICT_4X4_50,
            Objdetect.DICT_5X5_50,
            Objdetect.DICT_6X6_50,
            Objdetect.DICT_7X7_50
        )
        try {
            for ((index, code) in codes.withIndex()) {
                val image = Mat()
                val area = board.submat(Rect(50 + index % 2 * 250, 50 + index / 2 * 250, 120, 120))
                try {
                    Objdetect.generateImageMarker(
                        Objdetect.getPredefinedDictionary(code),
                        0,
                        120,
                        image
                    )
                    image.copyTo(area)
                } finally {
                    image.release()
                    area.release()
                }
            }
            val analyzer = ArucoAnalyzer()
            val frame = analyzer.processFrame(
                board,
                board,
                0L,
                0
            ).single() as VisionEvent.FrameMeasured
            assertEquals(MarkerFamily.entries.toSet(), frame.markers.map { it.family }.toSet())
            assertEquals(4, frame.markers.size)
            assertEquals(CameraParameterSource.UNAVAILABLE, frame.cameraParameters)
            assertTrue(frame.distances.isEmpty())
            // An explicit enum selection must keep the requested family instead of enabling all four.
            val selected = ArucoAnalyzer(ArucoDictionaryType.DICT_5X5_50).processFrame(
                board,
                board,
                0L,
                0
            ).single() as VisionEvent.FrameMeasured
            assertEquals(listOf(MarkerFamily.FIVE_BY_FIVE), selected.markers.map { it.family })
            assertEquals(0, selected.markers.single().markerId)
            board.setTo(Scalar(255.0))
            val cleared = analyzer.processFrame(
                board,
                board,
                1L,
                0
            ).single() as VisionEvent.FrameMeasured
            assertTrue(cleared.markers.isEmpty())
            assertTrue(cleared.distances.isEmpty())
        } finally {
            board.release()
        }
    }

    @Test
    fun sameImageScalesTranslationWithKnownPhysicalSize() {
        assertTrue(OpenCv.ensureInitialized())
        val model = CameraModel(640, 480, 800.0, 800.0, 320.0, 240.0, calibrated = true)
        // Synthetic 20 mm front-facing marker at camera coordinates (50, 20, 500) mm.
        val corners =
            listOf(Offset(384f, 256f), Offset(416f, 256f), Offset(416f, 288f), Offset(384f, 288f))
        val solver = MarkerPoseSolver()
        val small = solver.solve(corners, 20.0, model)
        assertNotNull(small)
        assertEquals(50.0, small!!.x, 0.01)
        assertEquals(20.0, small.y, 0.01)
        assertEquals(500.0, small.z, 0.01)

        // The same OpenCV pose solution must retain a real, normalized rotation for API mapping.
        val fullPose = solver.estimate(corners, 20.0, model)
        assertNotNull(fullPose)
        assertTrue(fullPose!!.orientation.isValid)
        val large = solver.solve(corners, 100.0, model)!!
        assertEquals(small.z * 5, large.z, 0.05)
    }
}
