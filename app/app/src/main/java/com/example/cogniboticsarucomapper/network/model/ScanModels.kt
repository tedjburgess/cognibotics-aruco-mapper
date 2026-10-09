package com.example.cogniboticsarucomapper.network.model

data class PositionDto(
    val x: Double,
    val y: Double,
    val z: Double
)

data class OrientationDto(
    val x: Double,
    val y: Double,
    val z: Double,
    val w: Double
)

data class ObservationRequest(
    val aruco_id: Int,
    val dictionary: String = "DICT_6X6_50",
    val name: String? = null,
    val position: PositionDto,
    val orientation: OrientationDto,
    val size_mm: Double? = null
)

data class RelationRequest(
    val from_aruco_id: Int,
    val from_dictionary: String = "DICT_6X6_50",
    val to_aruco_id: Int,
    val to_dictionary: String = "DICT_6X6_50",
    val distance_mm: Double,
    val relative_position: PositionDto? = null
)

data class ScanRequest(
    val site_name: String,
    val cell_name: String,
    val device_id: String,
    val measured_at: String,
    val observations: List<ObservationRequest>,
    val relations: List<RelationRequest> = emptyList()
)

data class ObservationResponse(
    val id: String,
    val marker_id: String,
    val aruco_id: Int,
    val dictionary: String,
    val name: String?,
    val position: PositionDto,
    val orientation: OrientationDto,
    val size_mm: Double?
)

data class RelationResponse(
    val id: String,
    val from_marker_id: String,
    val to_marker_id: String,
    val distance_mm: Double,
    val relative_position: PositionDto?
)

data class ScanResponse(
    val id: String,
    val site_name: String,
    val cell_name: String,
    val device_id: String,
    val measured_at: String,
    val created_at: String,
    val observations: List<ObservationResponse>,
    val relations: List<RelationResponse>
)