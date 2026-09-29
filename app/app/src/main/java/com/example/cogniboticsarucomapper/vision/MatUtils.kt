package com.example.cogniboticsarucomapper.vision

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Converts CameraX YUV_420_888 frames to OpenCV Mats.
 */
fun ImageProxy.toGrayMat(): Mat {
    val yPlane = planes[0]
    val gray = Mat(height, width, CvType.CV_8UC1)
    val buffer: ByteBuffer = yPlane.buffer
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

fun ImageProxy.toBgrMat(): Mat {
    val yuv = Mat(height + height / 2, width, CvType.CV_8UC1)
    val yPlane = planes[0]
    val yBuffer: ByteBuffer = yPlane.buffer
    if (yPlane.rowStride == width) {
        val yBytes = ByteArray(yBuffer.remaining())
        yBuffer.get(yBytes)
        yuv.put(0, 0, yBytes)
    } else {
        val yRow = ByteArray(width)
        for (row in 0 until height) {
            yBuffer.position(row * yPlane.rowStride)
            yBuffer.get(yRow, 0, width)
            yuv.put(row, 0, yRow)
        }
    }
    val uvRowStride = planes[1].rowStride
    val uvPixelStride = planes[1].pixelStride
    if (uvPixelStride == 1 && uvRowStride == width) {
        val uBuffer: ByteBuffer = planes[1].buffer
        val vBuffer: ByteBuffer = planes[2].buffer
        val uBytes = ByteArray(uBuffer.remaining())
        uBuffer.get(uBytes)
        val vBytes = ByteArray(vBuffer.remaining())
        vBuffer.get(vBytes)
        yuv.put(height, 0, uBytes)
        yuv.put(height + height / 4, 0, vBytes)
    } else {
        val uvRow = ByteArray(width)
        val uBuf = planes[1].buffer
        val vBuf = planes[2].buffer
        for (row in 0 until height / 2) {
            uBuf.position(row * uvRowStride)
            vBuf.position(row * planes[2].rowStride)
            var col = 0
            for (p in 0 until width / 2) {
                uvRow[col] = uBuf.get(p * uvPixelStride)
                uvRow[col + 1] = vBuf.get(p * uvPixelStride)
                col += 2
            }
            yuv.put(height + row, 0, uvRow)
        }
    }
    val bgr = Mat()
    Imgproc.cvtColor(yuv, bgr, Imgproc.COLOR_YUV2BGR_NV12)
    yuv.release()
    return bgr
}