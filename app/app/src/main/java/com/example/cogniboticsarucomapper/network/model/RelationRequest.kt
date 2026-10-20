package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class RelationRequest(
    @SerializedName("from_aruco_id") val fromArucoId: Int,
    @SerializedName("from_dictionary") val fromDictionary: String = "DICT_6X6_50",
    @SerializedName("to_aruco_id") val toArucoId: Int,
    @SerializedName("to_dictionary") val toDictionary: String = "DICT_6X6_50",
    @SerializedName("distance_mm") val distanceMm: Double,
    @SerializedName("relative_position") val relativePosition: PositionDto? = null
)
