package com.example.cogniboticsarucomapper.vision

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cogniboticsarucomapper.data.measurement.CameraModel
import com.example.cogniboticsarucomapper.data.measurement.MarkerPoseSolver
import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.MarkerOrigin
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AngledPoseTest {
    @Test
    fun knownSizePairsRemain135MmAcrossAnglesAndFamilies() {
        assertTrue(OpenCv.ensureInitialized())
        val model = CameraModel(640, 480, 800.0, 800.0, 320.0, 240.0, calibrated = true)
        val solver = MarkerPoseSolver()
        for (dictionary in ArucoDictionary.entries) {
            for (degrees in listOf(0.0, 15.0, 35.0, 60.0)) {
                val angle = Math.toRadians(degrees)

                // Project a flat sheet with its two centres 135 mm apart into a calibrated camera.
                fun corners(centre: Double): List<Offset> {
                    val half = dictionary.sizeMm / 2
                    return listOf(-half to half, half to half, half to -half, -half to -half)
                        .map { (x, localY) ->
                            val y = -(localY + centre) * cos(angle)
                            val z = 500 + (localY + centre) * sin(angle)
                            Offset((320 + 800 * x / z).toFloat(), (240 + 800 * y / z).toFloat())
                        }
                }
                val first = solver.estimate(
                    corners(-67.5),
                    dictionary.sizeMm,
                    model,
                    role = MarkerOrigin.CENTER,
                    gridCells = dictionary.markerGridCells
                )
                val second = solver.estimate(
                    corners(67.5),
                    dictionary.sizeMm,
                    model,
                    role = MarkerOrigin.CENTER,
                    gridCells = dictionary.markerGridCells
                )
                assertNotNull("${dictionary.label} at $degrees degrees", first)
                assertNotNull("${dictionary.label} at $degrees degrees", second)
                assertEquals(135.0, first!!.position.distanceTo(second!!.position), 0.1)
                assertEquals(degrees, first.tiltDegrees, 0.1)
            }
        }
    }

    @Test
    fun directionalMarkerReportsTopLeftInsteadOfCenter() {
        assertTrue(OpenCv.ensureInitialized())

        val model = CameraModel(
            640,
            480,
            800.0,
            800.0,
            320.0,
            240.0,
            calibrated = true
        )

        val solver = MarkerPoseSolver()
        val sizeMm = 40.0
        val half = sizeMm / 2.0

        val corners = listOf(
            Offset(288f, 208f),
            Offset(352f, 208f),
            Offset(352f, 272f),
            Offset(288f, 272f)
        )

        val positional = solver.estimate(
            corners,
            sizeMm,
            model,
            role = MarkerOrigin.CENTER
        )

        val directional = solver.estimate(
            corners,
            sizeMm,
            model,
            role = MarkerOrigin.CORNER
        )

        assertNotNull(positional)
        assertNotNull(directional)

        val positionalPosition = positional!!.position
        val directionalPosition = directional!!.position

        assertTrue(
            positionalPosition.distanceTo(directionalPosition) > half
        )
    }
}
