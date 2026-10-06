package com.example.cogniboticsarucomapper.data

import com.example.cogniboticsarucomapper.domain.MarkerPose
import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f
import org.opencv.core.Point3
import kotlin.math.sqrt

private const val CORNER_COUNT = 4

class PoseSolver {
    private val unitSquarePoints = MatOfPoint3f(
        Point3(-0.5, -0.5, 0.0),
        Point3(0.5, -0.5, 0.0),
        Point3(0.5, 0.5, 0.0),
        Point3(-0.5, 0.5, 0.0),
    )

    fun solve(
        corners: Mat,
        cameraMatrix: Mat,
        distortionCoefficients: MatOfDouble,
    ): MarkerPose? {
        val rotations = ArrayList<Mat>()
        val translations = ArrayList<Mat>()

        val solutionCount = try {
            Calib3d.solvePnPGeneric(
                unitSquarePoints,
                corners,
                cameraMatrix,
                distortionCoefficients,
                rotations,
                translations,
                false,
                Calib3d.SOLVEPNP_IPPE_SQUARE,
            )
        } catch (_: Exception) {
            return null
        }
        if (solutionCount <= 0) {
            return null
        }

        var bestInFront: MarkerPose? = null
        var deepest: MarkerPose? = null

        try {
            for (index in 0 until minOf(rotations.size, translations.size)) {
                val translation = readVector3(translations[index]) ?: continue

                val pose = MarkerPose(
                    tx = translation[0],
                    ty = translation[1],
                    tz = translation[2],
                    reprojectionErrorPx = reprojectionError(
                        corners = corners,
                        cameraMatrix = cameraMatrix,
                        distortionCoefficients = distortionCoefficients,
                        rotation = rotations[index],
                        translation = translations[index],
                    ),
                )

                if (deepest == null || pose.tz > deepest.tz) {
                    deepest = pose
                }

                if (pose.tz <= 0.0) {
                    continue
                }

                if (bestInFront == null || pose.reprojectionErrorPx < bestInFront.reprojectionErrorPx) {
                    bestInFront = pose
                }
            }
        } finally {
            releaseAll(rotations)
            releaseAll(translations)
        }

        if (bestInFront != null) {
            return bestInFront
        }
        
        return deepest
    }

    private fun reprojectionError(
        corners: Mat,
        cameraMatrix: Mat,
        distortionCoefficients: MatOfDouble,
        rotation: Mat,
        translation: Mat,
    ): Double {
        val projected = MatOfPoint2f()
        try {
            Calib3d.projectPoints(
                unitSquarePoints,
                rotation,
                translation,
                cameraMatrix,
                distortionCoefficients,
                projected,
            )

            var sumSquaredError = 0.0
            for (index in 0 until CORNER_COUNT) {
                val observed = readPoint(corners, index)
                val expected = readPoint(projected, index)
                val deltaX = observed[0] - expected[0]
                val deltaY = observed[1] - expected[1]
                sumSquaredError += deltaX * deltaX + deltaY * deltaY
            }

            return sqrt(sumSquaredError / CORNER_COUNT)
        } finally {
            projected.release()
        }
    }

    private fun readPoint(mat: Mat, index: Int): DoubleArray {
        if (mat.rows() == 1) {
            return mat.get(0, index)
        }

        return mat.get(index, 0)
    }

    private fun readVector3(mat: Mat): DoubleArray? {
        if (mat.rows() == 1 && mat.cols() >= 3) {
            return doubleArrayOf(mat.get(0, 0)[0], mat.get(0, 1)[0], mat.get(0, 2)[0])
        }
        if (mat.cols() == 1 && mat.rows() >= 3) {
            return doubleArrayOf(mat.get(0, 0)[0], mat.get(1, 0)[0], mat.get(2, 0)[0])
        }

        return null
    }

    private fun releaseAll(mats: List<Mat>) {
        for (mat in mats) {
            mat.release()
        }
    }
}
