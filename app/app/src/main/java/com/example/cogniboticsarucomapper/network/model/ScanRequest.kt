package com.example.cogniboticsarucomapper.network.model

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("site_name") val siteName: String,
    @SerializedName("cell_name") val cellName: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("measured_at") val measuredAt: String,
    val observations: List<ObservationRequest>,
    val relations: List<RelationRequest> = emptyList()
)
