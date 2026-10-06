package com.example.cogniboticsarucomapper.data

import org.opencv.android.OpenCVLoader

object OpenCv {
    @Volatile
    private var initialized: Boolean = false

    fun ensureInitialized(): Boolean {
        if (initialized) {
            return true
        }

        initialized = OpenCVLoader.initLocal()

        return initialized
    }
}
