package com.example.cogniboticsarucomapper.domain

import kotlin.math.abs

private const val MIN_NORMALISER = 1.0e-6

object MeasurementEngine {
    fun measure(
        reference: ReferenceMarker?,
        detections: List<MarkerDetection>,
    ): FrameMeasurement {
        if (detections.isEmpty()) {
            return FrameMeasurement(
                detectedCount = 0,
                referenceVisible = false,
                sizes = emptyList(),
                distances = emptyList(),
            )
        }

        if (reference == null) {
            return partial(detections.size)
        }

        val referenceKey = MarkerKey(reference.dictionary, reference.markerId)
        val anchor = findDetection(detections, referenceKey)

        if (anchor == null || anchor.pose.unitRangeMm <= 0.0) {
            return partial(detections.size)
        }

        val anchorDepthMm = anchor.pose.unitRangeMm * reference.sizeMm

        val sizes = ArrayList<MarkerSize>()
        val positions = HashMap<MarkerKey, Point3d>()

        for (detection in detections) {
            val detectionRangeMm = detection.pose.unitRangeMm
            if (detectionRangeMm <= 0.0) {
                continue
            }

            val depthMm = depthOnAnchorPlane(
                pose = detection.pose,
                anchorPose = anchor.pose,
                anchorSizeMm = reference.sizeMm,
                fallbackDepthMm = anchorDepthMm,
            )
            val sizeMm = depthMm / detectionRangeMm
            val key = MarkerKey(detection.dictionary, detection.markerId)

            sizes.add(
                MarkerSize(
                    dictionary = detection.dictionary,
                    markerId = detection.markerId,
                    sizeMm = sizeMm,
                    edgeLengthPx = detection.edgeLengthPx,
                ),
            )

            positions[key] = detection.pose.positionMm(sizeMm)
        }

        if (positions.isEmpty()) {
            return partial(detections.size)
        }

        sizes.sortWith(
            compareBy<MarkerSize> { it.dictionary }.thenBy { it.markerId },
        )

        val keys = ArrayList(positions.keys)
        keys.sortWith(
            compareBy<MarkerKey> { it.dictionary }.thenBy { it.markerId },
        )

        val distances = ArrayList<MarkerPairDistance>()
        for (leftIndex in keys.indices) {
            for (rightIndex in leftIndex + 1 until keys.size) {
                val left = keys[leftIndex]
                val right = keys[rightIndex]
                val leftPosition = positions.getValue(left)
                val rightPosition = positions.getValue(right)

                distances.add(
                    MarkerPairDistance(
                        from = left,
                        to = right,
                        distanceMm = leftPosition.distanceTo(rightPosition),
                    ),
                )
            }
        }

        return FrameMeasurement(
            detectedCount = detections.size,
            referenceVisible = true,
            sizes = sizes,
            distances = distances,
        )
    }

    private fun depthOnAnchorPlane(
        pose: MarkerPose,
        anchorPose: MarkerPose,
        anchorSizeMm: Double,
        fallbackDepthMm: Double,
    ): Double {
        val rangeMm = pose.unitRangeMm
        if (rangeMm <= 0.0) {
            return fallbackDepthMm
        }

        val anchorRangeMm = anchorPose.unitRangeMm
        if (anchorRangeMm <= 0.0) {
            return fallbackDepthMm
        }

        val directionX = pose.tx / rangeMm
        val directionY = pose.ty / rangeMm
        val directionZ = pose.tz / rangeMm

        val anchorX = anchorPose.tx * anchorSizeMm
        val anchorY = anchorPose.ty * anchorSizeMm
        val anchorZ = anchorPose.tz * anchorSizeMm

        val normaliser =
            directionX * anchorPose.normalX +
                directionY * anchorPose.normalY +
                directionZ * anchorPose.normalZ
        if (abs(normaliser) < MIN_NORMALISER) {
            return fallbackDepthMm
        }

        val offset =
            anchorX * anchorPose.normalX +
                anchorY * anchorPose.normalY +
                anchorZ * anchorPose.normalZ

        val depthMm = offset / normaliser
        if (depthMm <= 0.0) {
            return fallbackDepthMm
        }

        return depthMm
    }

    private fun partial(detectedCount: Int): FrameMeasurement {
        return FrameMeasurement(
            detectedCount = detectedCount,
            referenceVisible = false,
            sizes = emptyList(),
            distances = emptyList(),
        )
    }

    private fun findDetection(
        detections: List<MarkerDetection>,
        key: MarkerKey,
    ): MarkerDetection? {
        for (detection in detections) {
            val detectionKey = MarkerKey(detection.dictionary, detection.markerId)
            if (detectionKey == key) {
                return detection
            }
        }
        return null
    }
}
