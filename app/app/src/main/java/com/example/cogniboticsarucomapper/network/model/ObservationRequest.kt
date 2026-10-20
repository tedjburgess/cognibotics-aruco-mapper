package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class ObservationRequest(
    @SerializedName("aruco_id") val arucoId: Int,
    val dictionary: String = "DICT_6X6_50",
    val name: String? = null,
    val position: PositionDto,
    val orientation: OrientationDto,
    @SerializedName("size_mm") val sizeMm: Double? = null
)
