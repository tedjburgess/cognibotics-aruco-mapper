package com.example.cogniboticsarucomapper.network.model

data class SiteWithCellsResponse(val name: String, val cells: List<CellResponse> = emptyList())
