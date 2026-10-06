package com.example.cogniboticsarucomapper.data

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.cogniboticsarucomapper.domain.VisionEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Mat

private const val TAG = "VisionEngine"

class VisionEngine(
    private val analyzers: List<FrameAnalyzer> = emptyList(),
) : ImageAnalysis.Analyzer {
    interface FrameAnalyzer {
        fun processFrame(gray: Mat): List<VisionEvent>
    }

    private val _events = MutableSharedFlow<VisionEvent>(
        replay = 16,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<VisionEvent> = _events.asSharedFlow()

    private val _analysisEnabled = MutableStateFlow(true)
    val analysisEnabled: StateFlow<Boolean> = _analysisEnabled.asStateFlow()

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    private var fpsWindowStartMs = 0L
    private var fpsWindowFrames = 0

    fun setAnalysisEnabled(enabled: Boolean) {
        _analysisEnabled.value = enabled
    }

    override fun analyze(frame: ImageProxy) {
        try {
            if (!_analysisEnabled.value) {
                return
            }

            val now = System.currentTimeMillis()
            val gray = frame.toGrayMat()

            try {
                for (analyzer in analyzers) {
                    for (event in runAnalyzer(analyzer, gray)) {
                        _events.tryEmit(event)
                    }
                }
            } finally {
                gray.release()
            }

            updateFps(now)
        } catch (exception: Exception) {
            Log.e(TAG, "Frame processing failed", exception)
            _events.tryEmit(VisionEvent.AnalysisError.from(exception))
        } finally {
            frame.close()
        }
    }

    private fun runAnalyzer(analyzer: FrameAnalyzer, gray: Mat): List<VisionEvent> {
        try {
            return analyzer.processFrame(gray)
        } catch (exception: Exception) {
            Log.e(TAG, "Analyzer failed", exception)
            return listOf(VisionEvent.AnalysisError.from(exception))
        }
    }

    private fun updateFps(now: Long) {
        if (fpsWindowStartMs == 0L) {
            fpsWindowStartMs = now
        }

        fpsWindowFrames++

        val elapsed = now - fpsWindowStartMs
        if (elapsed >= 1000) {
            _fps.value = fpsWindowFrames * 1000f / elapsed
            fpsWindowStartMs = now
            fpsWindowFrames = 0
        }
    }
}
