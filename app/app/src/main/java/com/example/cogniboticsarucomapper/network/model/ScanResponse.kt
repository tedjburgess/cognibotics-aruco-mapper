package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class ScanResponse(
    val id: String,
    @SerializedName("site_name") val siteName: String,
    @SerializedName("cell_name") val cellName: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("created_at") val createdAt: String,
    val observations: List<ObservationResponse>,
    val relations: List<RelationResponse>
)
