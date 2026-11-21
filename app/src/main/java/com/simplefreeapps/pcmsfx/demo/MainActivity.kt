package com.simplefreeapps.pcmsfx.demo

import android.content.Context
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.simplefreeapps.pcmsfx.AndroidSfxPlayer
import com.simplefreeapps.pcmsfx.PcmSynthesizer
import com.simplefreeapps.pcmsfx.SfxPreset
import kotlin.math.roundToInt

private val BgTop = Color(0xFF070C18)
private val BgBot = Color(0xFF101B32)
private val CardBg = Color(0xFF141F36)
private val AccentMint = Color(0xFF9FF0CE)
private val AccentGold = Color(0xFFF2BD62)
private val SoftInk = Color(0xFFCBD5E1)

class MainActivity : AppCompatActivity() {
    private val sfxPlayer = AndroidSfxPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        val prefs = getSharedPreferences("compose_pcm_sfx_prefs", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("lang", "en") ?: "en"
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty || currentLocales.toLanguageTags() != savedLang) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AccentMint,
                    secondary = AccentGold,
                    background = BgTop,
                    surface = CardBg,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) {
                PcmSfxStudioScreen(
                    player = sfxPlayer,
                    onToggleLanguage = {
                        val next = if ((prefs.getString("lang", "en") ?: "en") == "ar") "en" else "ar"
                        prefs.edit().putString("lang", next).apply()
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
                    },
                )
            }
        }
    }
}

@Composable
private fun PcmSfxStudioScreen(
    player: AndroidSfxPlayer,
    onToggleLanguage: () -> Unit,
) {
    var activePreset by remember { mutableStateOf(SfxPreset.CONFIRM) }
    var pitch by remember { mutableFloatStateOf(1.0f) }
    var volume by remember { mutableFloatStateOf(1.0f) }
    var soundEnabled by remember { mutableStateOf(true) }

    val pcm = remember(activePreset, pitch) {
        PcmSynthesizer.presetPcm(activePreset, pitchMultiplier = pitch.toDouble())
    }
    val waveform = remember(pcm) {
        PcmSynthesizer.peakNormalizedWaveformPreview(pcm, buckets = 44)
    }
    val durationMs = remember(pcm) {
        (pcm.size * 1000L / PcmSynthesizer.DEFAULT_SAMPLE_RATE).toInt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBot)))
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = AccentMint.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = stringResource(R.string.badge_mit),
                        color = AccentMint,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onToggleLanguage) {
                        Text(stringResource(R.string.lang_toggle), color = AccentMint, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = {
                        soundEnabled = !soundEnabled
                        player.enabled = soundEnabled
                    }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = stringResource(if (soundEnabled) R.string.cd_sound_on else R.string.cd_sound_off),
                            tint = Color.White,
                        )
                    }
                }
            }

            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftInk,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF283A5E), RoundedCornerShape(18.dp)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = AccentMint)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.label_waveform_title, stringResource(PresetCatalog.labelRes(activePreset))),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = stringResource(R.string.label_waveform_meta_fmt, pcm.size, durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftInk,
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A111F))
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        val count = waveform.size.coerceAtLeast(1)
                        val slot = size.width / count
                        val barW = (slot * 0.62f).coerceAtLeast(2f)
                        val midY = size.height / 2f
                        waveform.forEachIndexed { index, mag ->
                            val h = (mag * size.height * 0.9f).coerceAtLeast(4f)
                            val x = index * slot + (slot - barW) / 2f
                            drawRoundRect(
                                color = if (index % 2 == 0) AccentMint else AccentGold,
                                topLeft = Offset(x, midY - h / 2f),
                                size = Size(barW, h),
                                cornerRadius = CornerRadius(4f, 4f),
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.label_pitch_fmt, pitch),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentMint, activeTrackColor = AccentMint),
                    )

                    Text(
                        text = stringResource(R.string.label_volume_fmt, (volume * 100f).roundToInt()),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            player.masterVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = AccentGold, activeTrackColor = AccentGold),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { player.play(activePreset, pitch.toDouble()) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint, contentColor = BgTop),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.btn_replay_active), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                pitch = 1.0f
                                volume = 1.0f
                                player.masterVolume = 1.0f
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.btn_reset_controls), maxLines = 1)
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.section_presets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            val rows = remember { SfxPreset.entries.chunked(3) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        chunk.forEach { preset ->
                            val selected = preset == activePreset
                            Surface(
                                color = if (selected) AccentMint.copy(alpha = 0.2f) else CardBg,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) AccentMint else Color(0xFF283A5E),
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                    .clickable {
                                        activePreset = preset
                                        player.play(preset, pitch.toDouble())
                                    },
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(PresetCatalog.labelRes(preset)),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) AccentMint else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.footer_note),
                style = MaterialTheme.typography.bodySmall,
                color = SoftInk,
            )
        }
    }
}
