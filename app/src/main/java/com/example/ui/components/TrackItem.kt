package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.example.ui.theme.YtRed

@Composable
fun TrackItem(
    track: TrackEntity,
    isPlayingThisTrack: Boolean = false,
    playlists: List<PlaylistEntity> = emptyList(),
    onTrackClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onOfflineToggle: () -> Unit,
    onAddToPlaylist: (Long) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .testTag("track_item_${track.id}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isPlayingThisTrack) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = YtRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isPlayingThisTrack) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isPlayingThisTrack) YtRed else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${track.artist} • ${track.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (track.isOfflineDownloaded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Offline Available",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (track.isUserUploaded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = YtRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "UPLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = YtRed,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Like toggle
            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier.testTag("like_button_${track.id}")
            ) {
                Icon(
                    imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like Track",
                    tint = if (track.isLiked) YtRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // More Options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (track.isOfflineDownloaded) "Remove Offline Download"
                                else "Download for Offline Playback"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (track.isOfflineDownloaded) Icons.Default.Delete else Icons.Outlined.DownloadForOffline,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOfflineToggle()
                        }
                    )

                    if (playlists.isNotEmpty()) {
                        Divider()
                        Text(
                            text = "Add to Playlist:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        playlists.forEach { playlist ->
                            DropdownMenuItem(
                                text = { Text(playlist.name) },
                                leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onAddToPlaylist(playlist.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
