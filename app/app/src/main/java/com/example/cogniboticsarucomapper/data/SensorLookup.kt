package com.example.cogniboticsarucomapper.data

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

fun readSensorGeometry(context: Context): SensorGeometry? {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null

    var backCameraId: String? = null
    for (cameraId in manager.cameraIdList) {
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (facing == CameraCharacteristics.LENS_FACING_BACK) {
            backCameraId = cameraId
            break
        }
    }

    if (backCameraId == null) {
        return null
    }

    val characteristics = manager.getCameraCharacteristics(backCameraId)
    val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
    val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
    
    if (focalLengths == null || focalLengths.isEmpty() || physicalSize == null) {
        return null
    }

    return SensorGeometry(
        focalLengthMm = focalLengths[0].toDouble(),
        sensorWidthMm = physicalSize.width.toDouble(),
        sensorHeightMm = physicalSize.height.toDouble(),
    )
}
