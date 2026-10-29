package com.example.cogniboticsarucomapper.data.measurement

import androidx.compose.ui.geometry.Offset
import com.example.cogniboticsarucomapper.domain.measurement.ImagePoint
import com.example.cogniboticsarucomapper.domain.measurement.MarkerImageQuality
import com.example.cogniboticsarucomapper.domain.measurement.MarkerRole
import com.example.cogniboticsarucomapper.domain.measurement.PositionMm
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f
import org.opencv.core.Point
import org.opencv.core.Point3

data class MarkerPoseEstimate(
    val position: PositionMm,
    val tiltDegrees: Double,
    val errorPixels: Double
)

// Each known-size marker is still solved independently in 3D.
class MarkerPoseSolver {
    fun solve(
        corners: List<Offset>,
        sizeMm: Double,
        model: CameraModel,
        role: MarkerRole
    ): PositionMm? = estimate(corners, sizeMm, model, role)?.position

    fun estimate(
        corners: List<Offset>,
        sizeMm: Double,
        model: CameraModel,
        role: MarkerRole,
        gridCells: Int = 6
    ): MarkerPoseEstimate? {
        val points = corners.map { ImagePoint(it.x.toDouble(), it.y.toDouble()) }
        if (!model.isValid ||
            !sizeMm.isFinite() ||
            sizeMm <= 0.0 ||
            !MarkerImageQuality.hasEnoughDetail(points, gridCells)
        ) {
            return null
        }
        val thickness = MarkerImageQuality.thicknessPixels(points)
        val half = sizeMm / 2.0
        // IPPE uses the decoded corner order and the marker's real dimensions.
        val objectPoints = MatOfPoint3f(
            Point3(-half, half, 0.0),
            Point3(half, half, 0.0),
            Point3(half, -half, 0.0),
            Point3(-half, -half, 0.0)
        )
        val imagePoints = MatOfPoint2f(*points.map { Point(it.x, it.y) }.toTypedArray())
        val camera = Mat.eye(3, 3, CvType.CV_64F)
        camera.put(0, 0, model.fx)
        camera.put(1, 1, model.fy)
        camera.put(0, 2, model.cx)
        camera.put(1, 2, model.cy)
        val distortion = MatOfDouble(*model.distortion.toDoubleArray())
        val rotations = mutableListOf<Mat>()
        val translations = mutableListOf<Mat>()
        val projected = MatOfPoint2f()
        val rotationMatrix = Mat()
        val iterativeRotation = Mat()
        val iterativeTranslation = Mat()
        try {
            Calib3d.solvePnPGeneric(
                objectPoints,
                imagePoints,
                camera,
                distortion,
                rotations,
                translations,
                false,
                Calib3d.SOLVEPNP_IPPE_SQUARE
            )
            val candidates = mutableListOf<MarkerPoseEstimate>()

            // Compare the original and refined solutions; refinement cannot make the chosen fit worse.
            fun score(rotation: Mat, translation: Mat) {
                val values = DoubleArray(3)
                translation.get(0, 0, values)

                val centerPosition = PositionMm(
                    values[0],
                    values[1],
                    values[2]
                )

                if (!centerPosition.isValid) return

                Calib3d.Rodrigues(rotation, rotationMatrix)

                val position =
                    if (role == MarkerRole.DIRECTIONAL_REFERENCE) {
                        // Marker-local top-left corner: (-half, +half, 0)
                        val localX = -half
                        val localY = half

                        val offsetX =
                            rotationMatrix.get(0, 0)[0] * localX +
                                rotationMatrix.get(0, 1)[0] * localY

                        val offsetY =
                            rotationMatrix.get(1, 0)[0] * localX +
                                rotationMatrix.get(1, 1)[0] * localY

                        val offsetZ =
                            rotationMatrix.get(2, 0)[0] * localX +
                                rotationMatrix.get(2, 1)[0] * localY

                        PositionMm(
                            centerPosition.x + offsetX,
                            centerPosition.y + offsetY,
                            centerPosition.z + offsetZ
                        )
                    } else {
                        centerPosition
                    }
                val zx = rotationMatrix.get(2, 0)[0]
                val zy = rotationMatrix.get(2, 1)[0]
                val zz = rotationMatrix.get(2, 2)[0]
                if (!zz.isFinite() ||
                    objectPoints.toArray().any { zx * it.x + zy * it.y + centerPosition.z <= 0.0 }
                ) {
                    return
                }
                Calib3d.projectPoints(
                    objectPoints,
                    rotation,
                    translation,
                    camera,
                    distortion,
                    projected
                )
                val expected = projected.toArray()
                if (expected.size != 4) return
                val squaredError = points.indices.sumOf { corner ->
                    val dx = points[corner].x - expected[corner].x
                    val dy = points[corner].y - expected[corner].y
                    dx * dx + dy * dy
                }
                val error = sqrt(squaredError / 4)
                // This checks the image fit, not the millimetre accuracy of the camera calibration.
                if (error.isFinite() && error <= minOf(2.0, thickness * 0.05)) {
                    candidates.add(
                        MarkerPoseEstimate(
                            position,
                            Math.toDegrees(acos(abs(zz).coerceIn(0.0, 1.0))),
                            error
                        )
                    )
                }
            }

            for (index in 0 until minOf(rotations.size, translations.size)) {
                score(rotations[index], translations[index])
                Calib3d.solvePnPRefineLM(
                    objectPoints,
                    imagePoints,
                    camera,
                    distortion,
                    rotations[index],
                    translations[index]
                )
                score(rotations[index], translations[index])
            }
            // An independent start handles the near-front-on cases where IPPE refinement gets stuck.
            if (Calib3d.solvePnP(
                    objectPoints,
                    imagePoints,
                    camera,
                    distortion,
                    iterativeRotation,
                    iterativeTranslation,
                    false,
                    Calib3d.SOLVEPNP_ITERATIVE
                )
            ) {
                score(iterativeRotation, iterativeTranslation)
            }
            val best = candidates.minByOrNull { it.errorPixels } ?: return null
            // Hide an ambiguous pose rather than pick between similarly fitting, widely different centres.
            if (candidates.any {
                    it.errorPixels <= best.errorPixels + 0.1 &&
                        it.position.distanceTo(best.position) > maxOf(5.0, best.position.z * 0.03)
                }
            ) {
                return null
            }
            return best
        } finally {
            objectPoints.release()
            imagePoints.release()
            camera.release()
            distortion.release()
            projected.release()
            rotationMatrix.release()
            iterativeRotation.release()
            iterativeTranslation.release()
            rotations.forEach { it.release() }
            translations.forEach { it.release() }
        }
    }
}
