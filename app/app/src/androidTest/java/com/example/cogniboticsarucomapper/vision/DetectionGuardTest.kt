package com.example.cogniboticsarucomapper.vision

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.objdetect.Objdetect

@RunWith(AndroidJUnit4::class)
class DetectionGuardTest {
    private fun withMarker(
        id: Int,
        corrupt: Boolean,
        dictionaryId: Int = Objdetect.DICT_4X4_50,
        check: (Mat) -> Unit
    ) {
        assertTrue(OpenCv.ensureInitialized())
        val board = Mat(240, 240, CvType.CV_8UC1, Scalar(255.0))
        val image = Mat()
        val area = board.submat(Rect(60, 60, 120, 120))
        try {
            Objdetect.generateImageMarker(
                Objdetect.getPredefinedDictionary(dictionaryId),
                id,
                120,
                image
            )
            if (corrupt) {
                // Flip one entire data cell; strict 4x4 decoding must not repair it into a marker.
                val cell = image.submat(Rect(40, 40, 20, 20))
                try {
                    Core.bitwise_not(cell, cell)
                } finally {
                    cell.release()
                }
            }
            image.copyTo(area)
            check(board)
        } finally {
            area.release()
            image.release()
            board.release()
        }
    }

    @Test
    fun supportedDictionaryAcceptsIdsAboveThree() {
        withMarker(42, false) { board ->
            val analyzer = ArucoAnalyzer()
            val markers = (
                analyzer.processFrame(
                    board,
                    board,
                    0,
                    0
                ).single() as VisionEvent.FrameMeasured
                ).markers
            assertTrue(
                markers.any {
                    it.dictionary == ArucoDictionary.DICT_4X4_50 && it.markerId == 42
                }
            )
        }
    }

    @Test
    fun supportedDictionaryRejectsIdsOutsideItsRange() {
        // A valid code from a larger dictionary must not become a supported 4x4 marker.
        withMarker(50, false, Objdetect.DICT_4X4_1000) { board ->
            val analyzer = ArucoAnalyzer()
            val frame = analyzer.processFrame(board, board, 0, 0)
                .single() as VisionEvent.FrameMeasured
            assertTrue(frame.markers.none { it.dictionary == ArucoDictionary.DICT_4X4_50 })
        }
    }

    @Test
    fun sevenBySeven44IsNotReportedAsFourByFour17() {
        withMarker(44, false, Objdetect.DICT_7X7_50) { board ->
            val frame = ArucoAnalyzer().processFrame(board, board, 0, 0)
                .single() as VisionEvent.FrameMeasured
            assertTrue(
                frame.markers.any {
                    it.dictionary == ArucoDictionary.DICT_7X7_50 && it.markerId == 44
                }
            )
            assertTrue(frame.markers.none { it.dictionary == ArucoDictionary.DICT_4X4_50 })
        }
    }

    @Test
    fun aRealFourByFour17RemainsDetectable() {
        withMarker(17, false) { board ->
            val frame = ArucoAnalyzer().processFrame(board, board, 0, 0)
                .single() as VisionEvent.FrameMeasured
            assertTrue(
                frame.markers.any {
                    it.dictionary == ArucoDictionary.DICT_4X4_50 && it.markerId == 17
                }
            )
        }
    }

    @Test
    fun originalDictionaryUsesItsOwnIdsAnd110MmSize() {
        withMarker(1023, false, Objdetect.DICT_ARUCO_ORIGINAL) { board ->
            val analyzer = ArucoAnalyzer().apply {
                calibratedCameraModel =
                    com.example.cogniboticsarucomapper.data.measurement.CameraModel(
                        240,
                        240,
                        300.0,
                        300.0,
                        120.0,
                        120.0,
                        calibrated = true
                    )
            }
            val frame = analyzer.processFrame(
                board,
                board,
                0,
                0
            ).single() as VisionEvent.FrameMeasured
            val marker = frame.markers.single {
                it.dictionary == ArucoDictionary.DICT_ARUCO_ORIGINAL && it.markerId == 1023
            }
            val position = requireNotNull(marker.positionMm)
            // 110 mm at a 300 px focal length spans approximately 120 px at 275 mm depth.
            org.junit.Assert.assertEquals(0.0, position.x, 1.0)
            org.junit.Assert.assertEquals(0.0, position.y, 1.0)
            org.junit.Assert.assertEquals(275.0, position.z, 5.0)
            assertTrue(
                frame.markers.none {
                    it.dictionary == ArucoDictionary.DICT_5X5_50 && it.markerId == 1023
                }
            )
        }
    }

    @Test
    fun damagedFourByFourCodeIsRejected() {
        withMarker(0, true) { board ->
            val frame = ArucoAnalyzer().processFrame(
                board,
                board,
                0,
                0
            ).single() as VisionEvent.FrameMeasured
            assertTrue(frame.markers.none { it.dictionary == ArucoDictionary.DICT_4X4_50 })
        }
    }
}
