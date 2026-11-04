package com.cognibotics.arucomapper.data.settings

import android.content.Context
import com.cognibotics.arucomapper.BuildConfig

/** Stores the API base URL, falling back to the value compiled into the build. */
class ApiSettingsStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): String = preferences.getString(KEY_BASE_URL, null)?.takeIf { it.isNotBlank() }
        ?: BuildConfig.API_BASE_URL

    fun save(baseUrl: String) {
        preferences.edit().putString(KEY_BASE_URL, baseUrl).apply()
    }

    fun reset() {
        preferences.edit().remove(KEY_BASE_URL).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "api_settings"
        const val KEY_BASE_URL = "base_url"
    }
}
