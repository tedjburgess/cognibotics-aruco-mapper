package com.example.cogniboticsarucomapper.network

import android.app.Application
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private lateinit var context: Context

    fun initialize(application: Application) {
        context = application.applicationContext
    }

    private fun serviceFor(url: String): ArucoApiService {
        val normalized = url.trim().let { if (it.endsWith("/")) it else "$it/" }
        require(normalized.startsWith("http://") || normalized.startsWith("https://")) {
            "API URL must begin with http:// or https://"
        }
        return Retrofit.Builder()
            .baseUrl(normalized)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArucoApiService::class.java)
    }

    val service: ArucoApiService
        get() = serviceFor(BackendSettings.getBaseUrl(context))
}
