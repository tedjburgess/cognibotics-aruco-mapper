package com.example.cogniboticsarucomapper.vision

import org.opencv.android.OpenCVLoader

object OpenCv {
    @Volatile
    private var initialized: Boolean = false

    /** Loads the OpenCV native library. Safe to call repeatedly. */
    fun ensureInitialized(): Boolean {
        if (initialized) return true
        initialized = OpenCVLoader.initLocal()
        return initialized
    }

    val isInitialized: Boolean get() = initialized
}