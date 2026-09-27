package com.example.cogniboticsarucomapper.vision

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Mat

/**
 * Central hub for OpenCV vision processing.
 *
 * CameraX feeds frames in via [analyze]; anyone interested in results collects
 * [events] (or subscribes via [addListener] for simple callbacks). New
 * detectors (e.g. ArUco) plug into [processFrame] — all consumers react
 * uniformly to [VisionEvent] emissions.
 */
class VisionEngine(
    // TODO: plug in the marker detection analyzer here, e.g.:
    //   listOf(ArUcoAnalyzer()) — a VisionEngine.FrameAnalyzer that finds
    //   markers in the frame and returns MarkerDetected/MarkersCleared events.
    private val analyzers: List<FrameAnalyzer> = emptyList(),
) : ImageAnalysis.Analyzer {

    interface FrameAnalyzer {
        /** Process one frame and return any events it produced. Called on the camera thread. */
        fun processFrame(gray: Mat, color: Mat, timestampMs: Long): List<VisionEvent>
    }

    private val _events = MutableSharedFlow<VisionEvent>(
        replay = 16,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<VisionEvent> = _events.asSharedFlow()

    private val _analysisEnabled = MutableStateFlow(true)
    val analysisEnabled: StateFlow<Boolean> = _analysisEnabled.asStateFlow()

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    private val listeners = mutableListOf<(VisionEvent) -> Unit>()

    private var lastFrameTimestampMs = 0L
    private var fpsWindowStartMs = 0L
    private var fpsWindowFrames = 0

    fun addListener(listener: (VisionEvent) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    fun setAnalysisEnabled(enabled: Boolean) {
        _analysisEnabled.value = enabled
    }

    override fun analyze(frame: ImageProxy) {
        val now = System.currentTimeMillis()
        try {
            if (!_analysisEnabled.value) return

            var gray: Mat? = null
            var color: Mat? = null
            try {
                gray = frame.toGrayMat()
                color = frame.toBgrMat()
                val produced = buildList {
                    for (analyzer in analyzers) {
                        addAll(runCatching { analyzer.processFrame(gray, color, now) }
                            .onFailure {
                                Log.e("VisionEngine", "Analyzer ${analyzer::class.simpleName} failed", it)
                                add(VisionEvent.AnalysisError(it.message ?: "analyzer failure", it))
                            }
                            .getOrDefault(emptyList()))
                    }
                }
                for (event in produced) {
                    _events.tryEmit(event)
                    listeners.forEach { it(event) }
                }
            } catch (t: Throwable) {
                Log.e("VisionEngine", "Frame processing failed", t)
                _events.tryEmit(VisionEvent.AnalysisError(t.message ?: "frame processing failed", t))
            } finally {
                gray?.release()
                color?.release()
            }

            updateFps(now)
            lastFrameTimestampMs = now
        } finally {
            frame.close()
        }
    }

    private fun updateFps(now: Long) {
        if (fpsWindowStartMs == 0L) fpsWindowStartMs = now
        fpsWindowFrames++
        val elapsed = now - fpsWindowStartMs
        if (elapsed >= 1000) {
            _fps.value = fpsWindowFrames * 1000f / elapsed
            fpsWindowStartMs = now
            fpsWindowFrames = 0
        }
    }

    companion object {
        private const val TAG = "VisionEngine"
    }
}