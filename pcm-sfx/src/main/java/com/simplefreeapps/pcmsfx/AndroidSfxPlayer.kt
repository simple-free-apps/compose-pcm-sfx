package com.simplefreeapps.pcmsfx

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Lightweight, zero-asset Android SFX player built over [AudioTrack] and [PcmSynthesizer].
 * Pre-renders default preset buffers on demand and plays asynchronously without UI blocking.
 */
class AndroidSfxPlayer(
    private val sampleRate: Int = PcmSynthesizer.DEFAULT_SAMPLE_RATE,
) {
    @Volatile
    var enabled: Boolean = true

    @Volatile
    var masterVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    private val cache = ConcurrentHashMap<SfxPreset, ShortArray>()

    /**
     * Plays a built-in [SfxPreset]. Uses a cached buffer when [pitchMultiplier] equals `1.0`.
     */
    fun play(preset: SfxPreset, pitchMultiplier: Double = 1.0) {
        if (!enabled) return
        val pcm = if (pitchMultiplier == 1.0) {
            cache.getOrPut(preset) { PcmSynthesizer.presetPcm(preset, sampleRate = sampleRate) }
        } else {
            PcmSynthesizer.presetPcm(preset, sampleRate = sampleRate, pitchMultiplier = pitchMultiplier)
        }
        playPcm(pcm)
    }

    /**
     * Plays any custom [ShortArray] 16-bit mono PCM buffer asynchronously.
     */
    fun playPcm(pcm: ShortArray) {
        if (!enabled || pcm.isEmpty()) return
        val vol = masterVolume
        if (vol <= 0f) return
        val scaled = if (vol >= 0.99f) pcm else ShortArray(pcm.size) { i ->
            (pcm[i] * vol).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        thread(isDaemon = true, name = "compose-pcm-sfx") {
            runCatching {
                val minBytes = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ).coerceAtLeast(scaled.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(scaled, 0, scaled.size)
                track.play()
                val durationMs = (scaled.size * 1000L / sampleRate) + 35L
                Thread.sleep(durationMs)
                track.stop()
                track.release()
            }
        }
    }
}
