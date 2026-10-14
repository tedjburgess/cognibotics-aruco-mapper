package com.example.cogniboticsarucomapper.data.calibration

import android.content.Context
import android.content.SharedPreferences

/** Camera parameters produced by a calibration run. Values are nullable until measured. */
data class CalibrationMeasurement(
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val focalLengthX: Double? = null,
    val focalLengthY: Double? = null,
    val principalPointX: Double? = null,
    val principalPointY: Double? = null,
    val radialDistortionK1: Double? = null,
    val radialDistortionK2: Double? = null,
    val radialDistortionK3: Double? = null,
    val tangentialDistortionP1: Double? = null,
    val tangentialDistortionP2: Double? = null,
    val reprojectionErrorPx: Double? = null,
    val updatedAtMillis: Long? = null
)

/** Persists the latest calibration output locally on this device. */
class CalibrationMeasurementStore(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): CalibrationMeasurement = with(preferences) {
        CalibrationMeasurement(
            imageWidth = getIntOrNull(KEY_IMAGE_WIDTH),
            imageHeight = getIntOrNull(KEY_IMAGE_HEIGHT),
            focalLengthX = getDouble(KEY_FOCAL_LENGTH_X),
            focalLengthY = getDouble(KEY_FOCAL_LENGTH_Y),
            principalPointX = getDouble(KEY_PRINCIPAL_POINT_X),
            principalPointY = getDouble(KEY_PRINCIPAL_POINT_Y),
            radialDistortionK1 = getDouble(KEY_RADIAL_K1),
            radialDistortionK2 = getDouble(KEY_RADIAL_K2),
            radialDistortionK3 = getDouble(KEY_RADIAL_K3),
            tangentialDistortionP1 = getDouble(KEY_TANGENTIAL_P1),
            tangentialDistortionP2 = getDouble(KEY_TANGENTIAL_P2),
            reprojectionErrorPx = getDouble(KEY_REPROJECTION_ERROR),
            updatedAtMillis = getLongOrNull(KEY_UPDATED_AT)
        )
    }

    /** Replaces the saved calibration. A null field is removed from storage. */
    fun update(measurement: CalibrationMeasurement) {
        preferences.edit()
            .putIntOrRemove(KEY_IMAGE_WIDTH, measurement.imageWidth)
            .putIntOrRemove(KEY_IMAGE_HEIGHT, measurement.imageHeight)
            .putDoubleOrRemove(KEY_FOCAL_LENGTH_X, measurement.focalLengthX)
            .putDoubleOrRemove(KEY_FOCAL_LENGTH_Y, measurement.focalLengthY)
            .putDoubleOrRemove(KEY_PRINCIPAL_POINT_X, measurement.principalPointX)
            .putDoubleOrRemove(KEY_PRINCIPAL_POINT_Y, measurement.principalPointY)
            .putDoubleOrRemove(KEY_RADIAL_K1, measurement.radialDistortionK1)
            .putDoubleOrRemove(KEY_RADIAL_K2, measurement.radialDistortionK2)
            .putDoubleOrRemove(KEY_RADIAL_K3, measurement.radialDistortionK3)
            .putDoubleOrRemove(KEY_TANGENTIAL_P1, measurement.tangentialDistortionP1)
            .putDoubleOrRemove(KEY_TANGENTIAL_P2, measurement.tangentialDistortionP2)
            .putDoubleOrRemove(KEY_REPROJECTION_ERROR, measurement.reprojectionErrorPx)
            .putLongOrRemove(KEY_UPDATED_AT, measurement.updatedAtMillis)
            .apply()
    }

    /** Clears all saved calibration values. */
    fun reset() {
        preferences.edit().clear().apply()
    }

    private fun SharedPreferences.getDouble(key: String): Double? =
        getString(key, null)?.toDoubleOrNull()

    private fun SharedPreferences.getIntOrNull(key: String): Int? =
        if (contains(key)) getInt(key, 0) else null

    private fun SharedPreferences.getLongOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private fun SharedPreferences.Editor.putDoubleOrRemove(key: String, value: Double?) = apply {
        if (value == null) remove(key) else putString(key, value.toString())
    }

    private fun SharedPreferences.Editor.putIntOrRemove(key: String, value: Int?) = apply {
        if (value == null) remove(key) else putInt(key, value)
    }

    private fun SharedPreferences.Editor.putLongOrRemove(key: String, value: Long?) = apply {
        if (value == null) remove(key) else putLong(key, value)
    }

    private companion object {
        const val PREFERENCES_NAME = "calibration_measurement"
        const val KEY_IMAGE_WIDTH = "image_width"
        const val KEY_IMAGE_HEIGHT = "image_height"
        const val KEY_FOCAL_LENGTH_X = "focal_length_x"
        const val KEY_FOCAL_LENGTH_Y = "focal_length_y"
        const val KEY_PRINCIPAL_POINT_X = "principal_point_x"
        const val KEY_PRINCIPAL_POINT_Y = "principal_point_y"
        const val KEY_RADIAL_K1 = "radial_distortion_k1"
        const val KEY_RADIAL_K2 = "radial_distortion_k2"
        const val KEY_RADIAL_K3 = "radial_distortion_k3"
        const val KEY_TANGENTIAL_P1 = "tangential_distortion_p1"
        const val KEY_TANGENTIAL_P2 = "tangential_distortion_p2"
        const val KEY_REPROJECTION_ERROR = "reprojection_error_px"
        const val KEY_UPDATED_AT = "updated_at_millis"
    }
}
