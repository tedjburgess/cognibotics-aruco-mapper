package com.example.cogniboticsarucomapper.ui.measurement

import com.example.cogniboticsarucomapper.domain.measurement.ArucoDictionary
import com.example.cogniboticsarucomapper.domain.measurement.MarkerKey
import com.example.cogniboticsarucomapper.domain.measurement.MarkerOrigin
import com.example.cogniboticsarucomapper.domain.measurement.role
import com.example.cogniboticsarucomapper.network.model.ObservationRequest
import com.example.cogniboticsarucomapper.network.model.OrientationDto
import com.example.cogniboticsarucomapper.network.model.PositionDto
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OriginalDictionaryMappingTest {
    @Test fun originalRetainsItsDictionaryAndSizeInApiJson() {
        val dictionary = ArucoDictionary.DICT_ARUCO_ORIGINAL
        val request = ObservationRequest(
            arucoId = 1023,
            dictionary = dictionary.name,
            position = PositionDto(0.0, 0.0, 275.0),
            orientation = OrientationDto(0.0, 0.0, 0.0, 1.0),
            sizeMm = dictionary.sizeMm
        )
        val json = Gson().toJsonTree(request).asJsonObject
        assertEquals("DICT_ARUCO_ORIGINAL", json["dictionary"].asString)
        assertEquals(1023, json["aruco_id"].asInt)
        assertEquals(110.0, json["size_mm"].asDouble, 0.0)
        assertNotEquals(MarkerKey(dictionary, 0), MarkerKey(ArucoDictionary.DICT_5X5_50, 0))
        assertEquals(MarkerOrigin.CORNER, MarkerKey(dictionary, 0).role())
        assertEquals(MarkerOrigin.CENTER, MarkerKey(dictionary, 10).role())
    }

    @Test fun historyKeepsSameNumericIdsInDifferentDictionariesSeparate() {
        val json = """{
            "id":"scan","site_name":"Lab","cell_name":"Cell","device_id":"Phone",
            "measured_at":"2026-01-01T00:00:00Z","created_at":"2026-01-01T00:00:00Z",
            "observations":[
              {"id":"a","marker_id":"original","aruco_id":10,"dictionary":"DICT_ARUCO_ORIGINAL"},
              {"id":"b","marker_id":"five","aruco_id":10,"dictionary":"DICT_5X5_50"}
            ],
            "relations":[{"id":"r","from_marker_id":"original","to_marker_id":"five","distance_mm":135.0}]
        }"""
        val result = Gson().fromJson(json, ScanResponse::class.java).toMeasurementHistoryItem()
        assertEquals("DICT_ARUCO_ORIGINAL #10", result.distances.single().fromLabel)
        assertEquals("DICT_5X5_50 #10", result.distances.single().toLabel)
        assertEquals(135.0, result.distances.single().distanceMm!!, 0.0)
    }
}
