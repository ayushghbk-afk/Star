package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.db.UserPreferencesEntity
import com.example.ui.components.TrackItem
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.YtRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    offlineTracks: List<TrackEntity>,
    userUploadedTracks: List<TrackEntity>,
    likedTracks: List<TrackEntity>,
    playlists: List<PlaylistEntity>,
    userPreferences: UserPreferencesEntity?,
    currentTrackId: Long?,
    onTrackSelect: (TrackEntity) -> Unit,
    onLikeToggle: (TrackEntity) -> Unit,
    onOfflineToggle: (TrackEntity) -> Unit,
    onTogglePremium: () -> Unit,
    onCreatePlaylist: (name: String, desc: String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onOpenUpload: () -> Unit,
    onOpenEqualizer: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Downloads, 1 = Playlists, 2 = My Uploads, 3 = Liked
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val isPremium = userPreferences?.isPremium ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library & Downloads",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = YtRed)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Premium Subscriber Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPremium) "Premium Member Active" else "Standard Streamer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPremium) "Unlimited offline downloads & 320kbps HD audio stream active" else "Upgrade to Premium for offline downloads & high quality playback",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isPremium,
                        onCheckedChange = { onTogglePremium() },
                        modifier = Modifier.testTag("premium_toggle_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Offline Downloads (${offlineTracks.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Playlists (${playlists.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("My Uploads (${userUploadedTracks.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Liked (${likedTracks.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Area
            when (selectedTab) {
                0 -> {
                    // Offline Downloads List
                    if (offlineTracks.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.CloudDownload,
                            title = "No Offline Downloads",
                            subtitle = "Tap the download icon on any song to listen without an internet connection."
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(offlineTracks) { track ->
                                TrackItem(
                                    track = track,
                                    isPlayingThisTrack = track.id == currentTrackId,
                                    playlists = playlists,
                                    onTrackClick = { onTrackSelect(track) },
                                    onLikeToggle = { onLikeToggle(track) },
                                    onOfflineToggle = { onOfflineToggle(track) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Playlists Section
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Playlists",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { showCreatePlaylistDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = YtRed)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Playlist")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (playlists.isEmpty()) {
                            EmptyLibraryState(
                                icon = Icons.Default.QueueMusic,
                                title = "No Custom Playlists",
                                subtitle = "Create your personal playlist to organize your favorite music."
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(playlists) { playlist ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            AsyncImage(
                                                model = playlist.coverUrl,
                                                contentDescription = playlist.name,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = playlist.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = playlist.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // My Server Uploads
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Uploaded & Server Processed Tracks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(onClick = onOpenUpload) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = YtRed)
                            }
                        }

                        if (userUploadedTracks.isEmpty()) {
                            EmptyLibraryState(
                                icon = Icons.Default.CloudUpload,
                                title = "No User Uploads Yet",
                                subtitle = "Upload MP3 or Video files. Our server will process them into high-quality streams with synchronized lyrics!"
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(userUploadedTracks) { track ->
                                    TrackItem(
                                        track = track,
                                        isPlayingThisTrack = track.id == currentTrackId,
                                        playlists = playlists,
                                        onTrackClick = { onTrackSelect(track) },
                                        onLikeToggle = { onLikeToggle(track) },
                                        onOfflineToggle = { onOfflineToggle(track) }
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Liked Songs
                    if (likedTracks.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.FavoriteBorder,
                            title = "No Liked Songs",
                            subtitle = "Heart your favorite songs to add them to your Liked collection."
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(likedTracks) { track ->
                                TrackItem(
                                    track = track,
                                    isPlayingThisTrack = track.id == currentTrackId,
                                    playlists = playlists,
                                    onTrackClick = { onTrackSelect(track) },
                                    onLikeToggle = { onLikeToggle(track) },
                                    onOfflineToggle = { onOfflineToggle(track) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, desc ->
                onCreatePlaylist(name, desc)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onCreate(name, desc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = YtRed)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
