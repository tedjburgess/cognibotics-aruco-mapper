package com.cognibotics.arucomapper.network

import com.cognibotics.arucomapper.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    @Volatile
    private var baseUrl: String = normalize(BuildConfig.API_BASE_URL)

    @Volatile
    private var service: ArucoApiService = create(baseUrl)

    fun currentBaseUrl(): String = baseUrl

    fun setBaseUrl(url: String) {
        if (url.isBlank()) return
        val normalized = normalize(url)
        if (normalized == baseUrl) return
        synchronized(this) {
            if (normalized != baseUrl) {
                baseUrl = normalized
                service = create(normalized)
            }
        }
    }

    fun getService(): ArucoApiService = service

    fun normalize(url: String): String = url.trim().trimEnd('/') + "/"

    private fun create(url: String): ArucoApiService = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ArucoApiService::class.java)
}
