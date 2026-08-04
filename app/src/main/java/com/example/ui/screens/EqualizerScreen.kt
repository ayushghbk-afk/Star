package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.YtRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    eqEnabled: Boolean,
    preset: String,
    band60Hz: Float,
    band230Hz: Float,
    band910Hz: Float,
    band3600Hz: Float,
    band14000Hz: Float,
    bassBoost: Float,
    virtualizer: Float,
    isPlaying: Boolean,
    onEqEnabledChange: (Boolean) -> Unit,
    onPresetSelect: (String) -> Unit,
    onBand60HzChange: (Float) -> Unit,
    onBand230HzChange: (Float) -> Unit,
    onBand910HzChange: (Float) -> Unit,
    onBand3600HzChange: (Float) -> Unit,
    onBand14000HzChange: (Float) -> Unit,
    onBassBoostChange: (Float) -> Unit,
    onVirtualizerChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf("Bass Booster", "Flat", "Vocal Booster", "Rock", "Electronic", "Hip-Hop")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("equalizer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = YtRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Built-in Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (eqEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (eqEnabled) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = onEqEnabledChange,
                        modifier = Modifier.testTag("eq_enable_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Spectrum Visualizer Canvas
            AnimatedSpectrumVisualizer(
                isPlaying = isPlaying && eqEnabled,
                bands = listOf(band60Hz, band230Hz, band910Hz, band3600Hz, band14000Hz)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Presets row
            Text(
                text = "Preset Mode",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { p ->
                    FilterChip(
                        selected = p == preset,
                        onClick = { onPresetSelect(p) },
                        label = { Text(p) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YtRed,
                            selectedLabelColor = Color.White
                        ),
                        enabled = eqEnabled
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5-Band Equalizer Sliders
            Text(
                text = "5-Band Spectrum (dB)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EqBandColumn("60Hz", band60Hz, -10f, 10f, eqEnabled, onBand60HzChange)
                EqBandColumn("230Hz", band230Hz, -10f, 10f, eqEnabled, onBand230HzChange)
                EqBandColumn("910Hz", band910Hz, -10f, 10f, eqEnabled, onBand910HzChange)
                EqBandColumn("3.6kHz", band3600Hz, -10f, 10f, eqEnabled, onBand3600HzChange)
                EqBandColumn("14kHz", band14000Hz, -10f, 10f, eqEnabled, onBand14000HzChange)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost & Virtualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bass Boost",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(bassBoost * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = YtRed
                        )
                        Slider(
                            value = bassBoost,
                            onValueChange = onBassBoostChange,
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(thumbColor = YtRed, activeTrackColor = YtRed)
                        )
                    }
                }

                // Virtualizer
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3D Virtualizer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(virtualizer * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = NeonCyan
                        )
                        Slider(
                            value = virtualizer,
                            onValueChange = onVirtualizerChange,
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun EqBandColumn(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "${if (value > 0) "+" else ""}${value.toInt()}dB",
            style = MaterialTheme.typography.labelSmall,
            color = if (value != 0f) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = min..max,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = if (value != 0f) YtRed else Color.Gray,
                    activeTrackColor = YtRed
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AnimatedSpectrumVisualizer(
    isPlaying: Boolean,
    bands: List<Float>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF141414)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val barCount = 20
            val barWidth = size.width / (barCount * 1.5f)
            val maxHeight = size.height

            for (i in 0 until barCount) {
                val bandFactor = (bands[i % bands.size] + 10f) / 20f
                val sine = if (isPlaying) kotlin.math.sin(phase + i * 0.4f) * 0.4f + 0.6f else 0.2f
                val barHeight = (maxHeight * bandFactor * sine.toFloat()).coerceIn(6f, maxHeight)

                val x = i * (barWidth * 1.5f)
                val y = maxHeight - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(YtRed, GoldAccent)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }
}
