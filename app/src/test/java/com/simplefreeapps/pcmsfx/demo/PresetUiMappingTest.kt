package com.simplefreeapps.pcmsfx.demo

import com.simplefreeapps.pcmsfx.SfxPreset
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetUiMappingTest {
    @Test
    fun everyPresetHasUniqueLocalizedResourceId() {
        val ids = SfxPreset.entries.map { PresetCatalog.labelRes(it) }.toSet()
        assertEquals(SfxPreset.entries.size, ids.size)
    }
}
