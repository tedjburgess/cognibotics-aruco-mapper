package com.example.cogniboticsarucomapper.data

import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat

fun ImageProxy.toGrayMat(): Mat {
    val yPlane = planes[0]
    val gray = Mat(height, width, CvType.CV_8UC1)
    val buffer = yPlane.buffer

    if (yPlane.rowStride == width) {
        val yBytes = ByteArray(buffer.remaining())
        buffer.get(yBytes)
        gray.put(0, 0, yBytes)
    } else {
        val rowBytes = ByteArray(width)
        for (row in 0 until height) {
            buffer.position(row * yPlane.rowStride)
            buffer.get(rowBytes, 0, width)
            gray.put(row, 0, rowBytes)
        }
    }

    return gray
}
