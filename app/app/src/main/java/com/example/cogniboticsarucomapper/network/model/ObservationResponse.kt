package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class ObservationResponse(
    val id: String,
    @SerializedName("marker_id") val markerId: String,
    @SerializedName("aruco_id") val arucoId: Int,
    val dictionary: String,
    val name: String?,
    val position: PositionDto,
    val orientation: OrientationDto,
    @SerializedName("size_mm") val sizeMm: Double?
)
