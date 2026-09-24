package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    /**
     * Mechanical roulette tick sound.
     */
    fun playTick() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 25
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val freq = 1200.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val envelope = exp(-t * 120.0) // fast decay
                    val sample = sin(2.0 * PI * freq * t) * envelope * 0.7
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Subtle sonar pulse when aiming at colors (pitch increases with accuracy).
     */
    fun playSonarPing(accuracy: Float) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 40
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val baseFreq = 440.0 + (accuracy * 6.0) // 440Hz -> 1040Hz

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val envelope = exp(-t * 60.0)
                    val sample = sin(2.0 * PI * baseFreq * t) * envelope * 0.4
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Triumphant victory fanfare when color is successfully captured!
     * 4-note ascending chord arpeggio with decay.
     */
    fun playVictoryChord() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
                val durationMs = 600
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                val noteIntervalSamples = (numSamples * 0.18).toInt()

                for (i in 0 until numSamples) {
                    var sampleSum = 0.0
                    for ((noteIdx, freq) in notes.withIndex()) {
                        val noteStart = noteIdx * noteIntervalSamples
                        if (i >= noteStart) {
                            val t = (i - noteStart).toDouble() / sampleRate
                            val envelope = exp(-t * 5.0)
                            sampleSum += (sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)) * envelope * 0.35
                        }
                    }
                    val clamped = sampleSum.coerceIn(-1.0, 1.0)
                    buffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Tactile vibration for tick / spin.
     */
    fun vibrateTick() {
        if (!isHapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Strong celebratory vibration when target is found.
     */
    fun vibrateVictory() {
        if (!isHapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 50, 150)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 80, 50, 150), -1)
            }
        } catch (_: Exception) {
        }
    }

    private var activeTrack: AudioTrack? = null
    private val trackLock = Any()

    private fun playBuffer(buffer: ShortArray) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = if (minBufferSize > 0) {
                maxOf(buffer.size * 2, minBufferSize)
            } else {
                buffer.size * 2
            }

            synchronized(trackLock) {
                try {
                    activeTrack?.stop()
                    activeTrack?.release()
                } catch (_: Throwable) {
                }
                activeTrack = null

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                activeTrack = track

                // Release track after playback completes
                val durationMs = (buffer.size * 1000L / sampleRate) + 50
                scope.launch {
                    kotlinx.coroutines.delay(durationMs)
                    synchronized(trackLock) {
                        if (activeTrack == track) {
                            try {
                                track.stop()
                                track.release()
                            } catch (_: Throwable) {
                            }
                            activeTrack = null
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // Audio hardware failure or track exhaustion must never crash the game
        }
    }
}
