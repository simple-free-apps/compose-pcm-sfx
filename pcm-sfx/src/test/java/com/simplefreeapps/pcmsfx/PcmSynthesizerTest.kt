package com.simplefreeapps.pcmsfx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PcmSynthesizerTest {

    @Test
    fun allPresetsProduceBoundedNonSilentPcmWithCleanTail() {
        for (preset in SfxPreset.entries) {
            val pcm = PcmSynthesizer.presetPcm(preset)
            assertTrue("Preset $preset must produce samples", pcm.isNotEmpty())
            val maxSample = pcm.maxOf { abs(it.toInt()) }
            assertTrue("Preset $preset must not be silent", maxSample > 500)
            assertTrue("Preset $preset must stay within 16-bit signed bounds", maxSample <= Short.MAX_VALUE.toInt())
            assertTrue(
                "Preset $preset last sample must fade near zero to prevent audio click",
                abs(pcm.last().toInt()) <= 250,
            )
        }
    }

    @Test
    fun waveformPreviewReturnsExpectedNormalizedBuckets() {
        val pcm = PcmSynthesizer.presetPcm(SfxPreset.VICTORY)
        val buckets = PcmSynthesizer.peakNormalizedWaveformPreview(pcm, buckets = 32)
        assertEquals(32, buckets.size)
        assertTrue(buckets.all { it in 0f..1f })
        assertTrue(buckets.any { it >= 0.99f })
    }

    @Test
    fun pitchMultiplierChangesFrequencyWithoutChangingDuration() {
        val base = PcmSynthesizer.presetPcm(SfxPreset.TAP, pitchMultiplier = 1.0)
        val high = PcmSynthesizer.presetPcm(SfxPreset.TAP, pitchMultiplier = 1.5)
        assertEquals(base.size, high.size)
        assertTrue(countZeroCrossings(high) > countZeroCrossings(base))
    }

    private fun countZeroCrossings(pcm: ShortArray): Int {
        var count = 0
        for (i in 1 until pcm.size) {
            if ((pcm[i - 1] < 0 && pcm[i] >= 0) || (pcm[i - 1] >= 0 && pcm[i] < 0)) {
                count++
            }
        }
        return count
    }
}
