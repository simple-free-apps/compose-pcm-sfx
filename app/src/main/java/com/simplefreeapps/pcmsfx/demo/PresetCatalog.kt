package com.simplefreeapps.pcmsfx.demo

import androidx.annotation.StringRes
import com.simplefreeapps.pcmsfx.SfxPreset

/**
 * Maps each [SfxPreset] to its localized string resource identifier.
 */
object PresetCatalog {
    @StringRes
    fun labelRes(preset: SfxPreset): Int = when (preset) {
        SfxPreset.TAP -> R.string.preset_tap
        SfxPreset.CONFIRM -> R.string.preset_confirm
        SfxPreset.ERROR -> R.string.preset_error
        SfxPreset.CHIME -> R.string.preset_chime
        SfxPreset.VICTORY -> R.string.preset_victory
        SfxPreset.COIN -> R.string.preset_coin
        SfxPreset.LASER -> R.string.preset_laser
        SfxPreset.CARD_FLIP -> R.string.preset_card_flip
        SfxPreset.POP -> R.string.preset_pop
    }
}
