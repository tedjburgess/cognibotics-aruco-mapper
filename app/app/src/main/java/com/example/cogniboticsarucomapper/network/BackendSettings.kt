package com.example.cogniboticsarucomapper.network

import android.content.Context

object BackendSettings {
    private const val PREFS_NAME = "api_preferences"
    private const val KEY_URL = "api_base_url"
    const val EMULATOR_URL = "http://10.0.2.2:8000/"

    fun getBaseUrl(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_URL, EMULATOR_URL) ?: EMULATOR_URL

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_URL, url).apply()
    }
}
