package com.cognibotics.arucomapper.network

import com.cognibotics.arucomapper.network.model.HealthResponse
import com.cognibotics.arucomapper.network.model.ScanRequest
import com.cognibotics.arucomapper.network.model.ScanResponse
import com.cognibotics.arucomapper.network.model.SiteWithCellsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("api/v1/scans/csv")
    suspend fun downloadScansCsv(@Query("cell_id") cellId: String): Response<ResponseBody>
}
