package com.example.gurultuolcer.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlin.math.log10

data class NoiseSample(
    val level: Float,
    val peak: Int,
    val read: Int,
    val recordingState: Int,
    val source: Int,
)

class NoiseMonitor(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: String? = null

    fun start() {
        if (recorder != null) return

        outputFile = "${context.cacheDir.absolutePath}/noise-meter-temp.m4a"
        recorder = createRecorder(outputFile!!).apply {
            prepare()
            start()
            maxAmplitude // Prime the amplitude reader; first value is often 0.
        }
    }

    fun readSample(): NoiseSample {
        val amplitude = recorder?.maxAmplitude?.coerceAtLeast(1) ?: 1
        val normalized = amplitude / 32767f
        val decibels = (20f * log10(normalized) + 86f).coerceIn(20f, 105f)

        return NoiseSample(
            level = decibels,
            peak = amplitude,
            read = if (recorder == null) -1 else 1,
            recordingState = if (recorder == null) 0 else 3,
            source = MediaRecorder.AudioSource.MIC,
        )
    }

    fun stop() {
        recorder?.runCatching { stop() }
        recorder?.release()
        recorder = null
    }

    private fun createRecorder(path: String): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(path)
        }
    }
}
