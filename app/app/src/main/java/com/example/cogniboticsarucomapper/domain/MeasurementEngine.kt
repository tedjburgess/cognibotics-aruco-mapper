package com.example.cogniboticsarucomapper.domain

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

        val sizes = ArrayList<MarkerSize>()
        val positions = HashMap<MarkerKey, Point3d>()

        for (detection in detections) {
            val ratio = detection.pose.unitRangeMm / anchor.pose.unitRangeMm
            val sizeMm = reference.sizeMm * ratio
            val key = MarkerKey(detection.dictionary, detection.markerId)

            sizes.add(
                MarkerSize(
                    dictionary = detection.dictionary,
                    markerId = detection.markerId,
                    sizeMm = sizeMm,
                    edgeLengthPx = detection.edgeLengthPx,
                ),
            )

            positions[key] = Point3d(
                x = detection.pose.tx * sizeMm,
                y = detection.pose.ty * sizeMm,
                z = detection.pose.tz * sizeMm,
            )
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
