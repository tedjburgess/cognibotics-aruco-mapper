package com.example.cogniboticsarucomapper.network

import com.example.cogniboticsarucomapper.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trim().trimEnd('/') + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getService(): ArucoApiService = retrofit.create(ArucoApiService::class.java)
}
