package com.simplefreeapps.pcmsfx

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

/**
 * Available procedural waveforms synthesized mathematically without bundled audio files.
 */
enum class WaveformType {
    SINE,
    TRIANGLE,
    SQUARE_SOFT,
    WHITE_NOISE,
}

/**
 * Built-in UI and mobile game sound presets synthesized in pure Kotlin.
 */
enum class SfxPreset {
    TAP,
    CONFIRM,
    ERROR,
    CHIME,
    VICTORY,
    COIN,
    LASER,
    CARD_FLIP,
    POP,
}

/**
 * Amplitude envelope parameters normalized over the buffer duration.
 */
data class AdsrEnvelope(
    val attackRatio: Double = 0.04,
    val decayRate: Double = 2.0,
    val tailFadeRatio: Double = 0.12,
)

/**
 * Specification for synthesizing a single 16-bit mono PCM sweep or tone.
 */
data class SfxSpec(
    val durationMs: Int,
    val startHz: Double,
    val endHz: Double = startHz,
    val waveform: WaveformType = WaveformType.SINE,
    val gain: Double = 0.28,
    val harmonics: DoubleArray = doubleArrayOf(1.0),
    val envelope: AdsrEnvelope = AdsrEnvelope(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SfxSpec) return false
        return durationMs == other.durationMs &&
            startHz == other.startHz &&
            endHz == other.endHz &&
            waveform == other.waveform &&
            gain == other.gain &&
            harmonics.contentEquals(other.harmonics) &&
            envelope == other.envelope
    }

    override fun hashCode(): Int {
        var result = durationMs
        result = 31 * result + startHz.hashCode()
        result = 31 * result + endHz.hashCode()
        result = 31 * result + waveform.hashCode()
        result = 31 * result + gain.hashCode()
        result = 31 * result + harmonics.contentHashCode()
        result = 31 * result + envelope.hashCode()
        return result
    }
}

/**
 * Pure-JVM procedural 16-bit PCM synthesizer for Android UI and game sound effects.
 */
object PcmSynthesizer {
    const val DEFAULT_SAMPLE_RATE = 44100

    /**
     * Synthesizes a mono 16-bit PCM buffer from [spec].
     */
    fun synthesize(
        spec: SfxSpec,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        pitchMultiplier: Double = 1.0,
    ): ShortArray {
        val count = ((sampleRate.toLong() * spec.durationMs) / 1000L).toInt().coerceAtLeast(1)
        val pitch = pitchMultiplier.coerceIn(0.25, 4.0)
        val harmonicsSum = spec.harmonics.sum().takeIf { it > 0.0 } ?: 1.0
        var phase = 0.0
        var noiseState = 0x5EED1234

        return ShortArray(count) { idx ->
            val progress = idx.toDouble() / count
            val freq = (spec.startHz + (spec.endHz - spec.startHz) * progress) * pitch
            phase += (2.0 * PI * freq) / sampleRate

            var raw = 0.0
            for (hIdx in spec.harmonics.indices) {
                val weight = spec.harmonics[hIdx]
                if (weight == 0.0) continue
                val harmonicPhase = phase * (hIdx + 1)
                val sample = when (spec.waveform) {
                    WaveformType.SINE -> sin(harmonicPhase)
                    WaveformType.TRIANGLE -> {
                        val norm = ((harmonicPhase / (2.0 * PI)) % 1.0 + 1.0) % 1.0
                        1.0 - 4.0 * abs(norm - 0.5)
                    }
                    WaveformType.SQUARE_SOFT -> {
                        val s = sin(harmonicPhase)
                        (s * 1.4).coerceIn(-0.85, 0.85)
                    }
                    WaveformType.WHITE_NOISE -> {
                        noiseState = noiseState xor (noiseState shl 13)
                        noiseState = noiseState xor (noiseState ushr 17)
                        noiseState = noiseState xor (noiseState shl 5)
                        (noiseState.toDouble() / Int.MAX_VALUE) * 0.7
                    }
                }
                raw += sample * weight
            }

            val env = computeEnvelope(progress, spec.envelope)
            val scaled = (raw / harmonicsSum) * env * spec.gain.coerceIn(0.0, 1.0) * Short.MAX_VALUE
            scaled.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /**
     * Synthesizes a multi-frequency chord with smooth attack and decay.
     */
    fun synthesizeChord(
        freqsHz: DoubleArray,
        durationMs: Int,
        gain: Double = 0.25,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        pitchMultiplier: Double = 1.0,
    ): ShortArray {
        val count = ((sampleRate.toLong() * durationMs) / 1000L).toInt().coerceAtLeast(1)
        if (freqsHz.isEmpty()) return ShortArray(count)
        val pitch = pitchMultiplier.coerceIn(0.25, 4.0)
        val envSpec = AdsrEnvelope(attackRatio = 0.05, decayRate = 2.1, tailFadeRatio = 0.2)

        return ShortArray(count) { idx ->
            val t = idx.toDouble() / sampleRate
            val progress = idx.toDouble() / count
            var sum = 0.0
            for (f in freqsHz) {
                sum += sin(2.0 * PI * (f * pitch) * t)
            }
            val env = computeEnvelope(progress, envSpec)
            val v = (sum / freqsHz.size) * env * gain.coerceIn(0.0, 1.0) * Short.MAX_VALUE
            v.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /**
     * Synthesizes a sequential arpeggio across [notesHz].
     */
    fun synthesizeSequence(
        notesHz: DoubleArray,
        noteMs: Int,
        gain: Double = 0.26,
        waveform: WaveformType = WaveformType.SINE,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        pitchMultiplier: Double = 1.0,
    ): ShortArray {
        if (notesHz.isEmpty()) return ShortArray(0)
        val noteBuffers = notesHz.map { hz ->
            synthesize(
                spec = SfxSpec(
                    durationMs = noteMs,
                    startHz = hz,
                    endHz = hz,
                    waveform = waveform,
                    gain = gain,
                    harmonics = doubleArrayOf(1.0, 0.25),
                    envelope = AdsrEnvelope(attackRatio = 0.05, decayRate = 1.4, tailFadeRatio = 0.18),
                ),
                sampleRate = sampleRate,
                pitchMultiplier = pitchMultiplier,
            )
        }
        val total = noteBuffers.sumOf { it.size }
        val out = ShortArray(total)
        var offset = 0
        for (buf in noteBuffers) {
            buf.copyInto(out, destinationOffset = offset)
            offset += buf.size
        }
        return out
    }

    /**
     * Generates PCM audio for any built-in [SfxPreset].
     */
    fun presetPcm(
        preset: SfxPreset,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        pitchMultiplier: Double = 1.0,
    ): ShortArray = when (preset) {
        SfxPreset.TAP -> synthesize(
            SfxSpec(
                durationMs = 42,
                startHz = 780.0,
                endHz = 520.0,
                waveform = WaveformType.TRIANGLE,
                gain = 0.30,
                envelope = AdsrEnvelope(attackRatio = 0.03, decayRate = 3.2, tailFadeRatio = 0.25),
            ),
            sampleRate,
            pitchMultiplier,
        )
        SfxPreset.CONFIRM -> synthesizeChord(
            freqsHz = doubleArrayOf(659.25, 987.77, 1318.51),
            durationMs = 135,
            gain = 0.26,
            sampleRate = sampleRate,
            pitchMultiplier = pitchMultiplier,
        )
        SfxPreset.ERROR -> synthesizeSequence(
            notesHz = doubleArrayOf(240.0, 175.0),
            noteMs = 95,
            gain = 0.24,
            waveform = WaveformType.SQUARE_SOFT,
            sampleRate = sampleRate,
            pitchMultiplier = pitchMultiplier,
        )
        SfxPreset.CHIME -> synthesizeSequence(
            notesHz = doubleArrayOf(880.0, 1174.66),
            noteMs = 85,
            gain = 0.24,
            waveform = WaveformType.SINE,
            sampleRate = sampleRate,
            pitchMultiplier = pitchMultiplier,
        )
        SfxPreset.VICTORY -> synthesizeSequence(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
            noteMs = 100,
            gain = 0.28,
            waveform = WaveformType.TRIANGLE,
            sampleRate = sampleRate,
            pitchMultiplier = pitchMultiplier,
        )
        SfxPreset.COIN -> synthesizeSequence(
            notesHz = doubleArrayOf(987.77, 1318.51),
            noteMs = 75,
            gain = 0.25,
            waveform = WaveformType.SINE,
            sampleRate = sampleRate,
            pitchMultiplier = pitchMultiplier,
        )
        SfxPreset.LASER -> synthesize(
            SfxSpec(
                durationMs = 160,
                startHz = 1350.0,
                endHz = 210.0,
                waveform = WaveformType.SQUARE_SOFT,
                gain = 0.22,
                harmonics = doubleArrayOf(1.0, 0.35),
                envelope = AdsrEnvelope(attackRatio = 0.03, decayRate = 2.4, tailFadeRatio = 0.2),
            ),
            sampleRate,
            pitchMultiplier,
        )
        SfxPreset.CARD_FLIP -> synthesize(
            SfxSpec(
                durationMs = 55,
                startHz = 420.0,
                endHz = 720.0,
                waveform = WaveformType.WHITE_NOISE,
                gain = 0.24,
                envelope = AdsrEnvelope(attackRatio = 0.12, decayRate = 2.8, tailFadeRatio = 0.3),
            ),
            sampleRate,
            pitchMultiplier,
        )
        SfxPreset.POP -> synthesize(
            SfxSpec(
                durationMs = 65,
                startHz = 340.0,
                endHz = 890.0,
                waveform = WaveformType.SINE,
                gain = 0.32,
                envelope = AdsrEnvelope(attackRatio = 0.05, decayRate = 2.9, tailFadeRatio = 0.25),
            ),
            sampleRate,
            pitchMultiplier,
        )
    }

    /**
     * Downsamples a PCM array into [buckets] normalized peak magnitudes in `0f..1f` for Canvas previews.
     */
    fun peakNormalizedWaveformPreview(pcm: ShortArray, buckets: Int = 48): List<Float> {
        if (buckets <= 0) return emptyList()
        if (pcm.isEmpty()) return List(buckets) { 0f }
        val step = max(1, pcm.size / buckets)
        val rawPeaks = FloatArray(buckets) { b ->
            val start = (b * pcm.size) / buckets
            val end = (((b + 1) * pcm.size) / buckets).coerceAtLeast(start + 1).coerceAtMost(pcm.size)
            var peak = 0
            for (i in start until end) {
                val a = abs(pcm[i].toInt())
                if (a > peak) peak = a
            }
            peak.toFloat()
        }
        val maxPeak = rawPeaks.maxOrNull()?.takeIf { it > 0f } ?: 1f
        return rawPeaks.map { (it / maxPeak).coerceIn(0f, 1f) }
    }

    private fun computeEnvelope(progress: Double, env: AdsrEnvelope): Double {
        val attack = if (env.attackRatio > 0.0 && progress < env.attackRatio) {
            progress / env.attackRatio
        } else {
            1.0
        }
        val decay = exp(-env.decayRate * progress)
        val tailStart = (1.0 - env.tailFadeRatio).coerceIn(0.0, 1.0)
        val tail = if (env.tailFadeRatio > 0.0 && progress > tailStart) {
            ((1.0 - progress) / env.tailFadeRatio).coerceIn(0.0, 1.0)
        } else {
            1.0
        }
        return (attack * decay * tail).coerceIn(0.0, 1.0)
    }
}
