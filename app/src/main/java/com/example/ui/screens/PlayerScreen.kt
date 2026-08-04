package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.TrackEntity
import com.example.ui.components.RealtimeSyncLyricView
import com.example.ui.model.LyricLine
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.YtRed
import com.example.ui.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    track: TrackEntity,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    durationMs: Long,
    parsedLyrics: List<LyricLine>,
    activeLyricIndex: Int,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onLikeToggle: () -> Unit,
    onOfflineToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cover/Song, 1 = Realtime Lyrics

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkBackground,
        modifier = Modifier.testTag("full_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2B0A0E),
                            DarkBackground,
                            DarkBackground
                        )
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("player_collapse_btn")) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Tab Selector (Song | Lyrics)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.width(180.dp)
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Song", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Lyrics", fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("open_eq_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = YtRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main View Area (Cover or Lyrics)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTab == 0) {
                    // Song Cover View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            shadowElevation = 16.dp
                        ) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stream Quality Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = "HD",
                                    tint = YtRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (track.isOfflineDownloaded) "OFFLINE HI-RES FLAC" else "LIVE STREAM 320 KBPS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Real-time Lyrics View
                    RealtimeSyncLyricView(
                        lyrics = parsedLyrics,
                        activeLyricIndex = activeLyricIndex,
                        onLyricLineClick = onSeekTo
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Song Info & Actions (Like, Download)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} • ${track.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Download Button
                IconButton(
                    onClick = onOfflineToggle,
                    modifier = Modifier.testTag("player_offline_toggle")
                ) {
                    Icon(
                        imageVector = if (track.isOfflineDownloaded) Icons.Default.DownloadDone else Icons.Outlined.DownloadForOffline,
                        contentDescription = "Download Offline",
                        tint = if (track.isOfflineDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Like Button
                IconButton(
                    onClick = onLikeToggle,
                    modifier = Modifier.testTag("player_like_toggle")
                ) {
                    Icon(
                        imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Track",
                        tint = if (track.isLiked) YtRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Slider & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playbackPositionMs.toFloat(),
                    onValueChange = { onSeekTo(it.toLong()) },
                    valueRange = 0f..durationMs.toFloat().coerceAtLeast(1000f),
                    colors = SliderDefaults.colors(
                        thumbColor = YtRed,
                        activeTrackColor = YtRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("player_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(playbackPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMs(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle button
                IconButton(onClick = onShuffleToggle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Previous
                IconButton(onClick = onPrevClick) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Main Play / Pause Circle
                Surface(
                    onClick = onPlayPauseClick,
                    shape = CircleShape,
                    color = YtRed,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("player_main_play_pause")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(onClick = onRepeatToggle) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.OFF -> Icons.Default.Repeat
                            RepeatMode.ALL -> Icons.Default.Repeat
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
