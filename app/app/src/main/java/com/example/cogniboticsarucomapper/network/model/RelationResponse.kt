package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class RelationResponse(
    val id: String,
    @SerializedName("from_marker_id") val fromMarkerId: String,
    @SerializedName("to_marker_id") val toMarkerId: String,
    @SerializedName("distance_mm") val distanceMm: Double,
    @SerializedName("relative_position") val relativePosition: PositionDto?
)
