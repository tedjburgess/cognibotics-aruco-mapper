package com.example.cogniboticsarucomapper.network

import com.example.cogniboticsarucomapper.network.model.HealthResponse
import com.example.cogniboticsarucomapper.network.model.ScanRequest
import com.example.cogniboticsarucomapper.network.model.ScanResponse
import com.example.cogniboticsarucomapper.network.model.SiteWithCellsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Query

interface ArucoApiService {

    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/v1/scans")
    suspend fun getScans(): List<ScanResponse>

    @POST("api/v1/scans")
    suspend fun createScan(@Body scan: ScanRequest): ScanResponse

    @GET("api/v1/sites")
    suspend fun getSites(): List<SiteWithCellsResponse>

    @GET("api/v1/scans/csv")
    suspend fun downloadScansCsv(
        @Query("site_name") siteName: String,
        @Query("cell_name") cellName: String
    ): Response<ResponseBody>
}
