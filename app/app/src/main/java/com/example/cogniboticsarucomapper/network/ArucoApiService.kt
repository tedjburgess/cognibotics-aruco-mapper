package com.example.cogniboticsarucomapper.network

import com.example.cogniboticsarucomapper.network.model.HealthResponse
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.network.model.SiteWithCellsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ArucoApiService {

    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/v1/scans")
    suspend fun getScans(): List<ScanResponse>

    @DELETE("api/v1/scans/{id}")
    suspend fun deleteScan(@Path("id") id: String)

    @POST("api/v1/scans")
    suspend fun createScan(@Body scan: ScanRequest): ScanResponse

    @GET("api/v1/sites")
    suspend fun getSites(): List<SiteWithCellsResponse>
}
