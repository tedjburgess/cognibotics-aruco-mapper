package com.example.cogniboticsarucomapper.network

import com.example.cogniboticsarucomapper.network.model.HealthResponse
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ArucoApiService {

    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/v1/scans")
    suspend fun getScans(): List<ScanResponse>

    @POST("api/v1/scans")
    suspend fun createScan(
        @Body scan: ScanRequest
    ): ScanResponse
}