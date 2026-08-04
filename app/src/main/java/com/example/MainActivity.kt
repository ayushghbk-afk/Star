package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YtRed
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MusicViewModel) {
    var selectedScreen by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Community, 2 = Library

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val offlineTracks by viewModel.offlineTracks.collectAsStateWithLifecycle()
    val userUploadedTracks by viewModel.userUploadedTracks.collectAsStateWithLifecycle()
    val likedTracks by viewModel.likedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val parsedLyrics by viewModel.parsedLyrics.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()

    val showFullPlayer by viewModel.showFullPlayer.collectAsStateWithLifecycle()
    val showEqualizer by viewModel.showEqualizer.collectAsStateWithLifecycle()
    val showUploadSheet by viewModel.showUploadSheet.collectAsStateWithLifecycle()

    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()
    val uploadStatusMessage by viewModel.uploadStatusMessage.collectAsStateWithLifecycle()
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    val eqEnabled by viewModel.eqEnabled.collectAsStateWithLifecycle()
    val eqPreset by viewModel.eqPreset.collectAsStateWithLifecycle()
    val band60Hz by viewModel.band60Hz.collectAsStateWithLifecycle()
    val band230Hz by viewModel.band230Hz.collectAsStateWithLifecycle()
    val band910Hz by viewModel.band910Hz.collectAsStateWithLifecycle()
    val band3600Hz by viewModel.band3600Hz.collectAsStateWithLifecycle()
    val band14000Hz by viewModel.band14000Hz.collectAsStateWithLifecycle()
    val bassBoost by viewModel.bassBoost.collectAsStateWithLifecycle()
    val virtualizer by viewModel.virtualizer.collectAsStateWithLifecycle()

    val progressFraction = if (durationMs > 0) playbackPositionMs.toFloat() / durationMs.toFloat() else 0f

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Persistent Mini Player Bar
                currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        progressFraction = progressFraction,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onSkipNextClick = { viewModel.playNextTrack() },
                        onExpandClick = { viewModel.toggleFullPlayer(true) }
                    )
                }

                // M3 Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedScreen == 0,
                        onClick = { selectedScreen = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Explore") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = YtRed,
                            indicatorColor = YtRed
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 1,
                        onClick = { selectedScreen = 1 },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Community") },
                        label = { Text("Community") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = YtRed,
                            indicatorColor = YtRed
                        ),
                        modifier = Modifier.testTag("nav_item_community")
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 2,
                        onClick = { selectedScreen = 2 },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = YtRed,
                            indicatorColor = YtRed
                        ),
                        modifier = Modifier.testTag("nav_item_library")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreen) {
                0 -> HomeScreen(
                    tracks = allTracks,
                    currentTrackId = currentTrack?.id,
                    searchQuery = searchQuery,
                    playlists = playlists,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onTrackSelect = { viewModel.playTrackNow(it) },
                    onLikeToggle = { viewModel.toggleLikeTrack(it) },
                    onOfflineToggle = { viewModel.toggleOfflineDownload(it) },
                    onAddToPlaylist = { pId, tId -> viewModel.addTrackToPlaylist(pId, tId) },
                    onOpenUpload = { viewModel.toggleUploadSheet(true) }
                )

                1 -> CommunityScreen(
                    posts = communityPosts,
                    tracks = allTracks,
                    onPostLikeToggle = { viewModel.togglePostLike(it) },
                    onTrackSelect = { viewModel.playTrackNow(it) },
                    onCreatePost = { author, text, tId -> viewModel.createPost(author, text, tId) },
                    onAddComment = { pId, author, text -> viewModel.addComment(pId, author, text) }
                )

                2 -> LibraryScreen(
                    offlineTracks = offlineTracks,
                    userUploadedTracks = userUploadedTracks,
                    likedTracks = likedTracks,
                    playlists = playlists,
                    userPreferences = userPreferences,
                    currentTrackId = currentTrack?.id,
                    onTrackSelect = { viewModel.playTrackNow(it) },
                    onLikeToggle = { viewModel.toggleLikeTrack(it) },
                    onOfflineToggle = { viewModel.toggleOfflineDownload(it) },
                    onTogglePremium = { viewModel.togglePremiumSubscriber() },
                    onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                    onDeletePlaylist = { pId -> viewModel.deletePlaylist(pId) },
                    onOpenUpload = { viewModel.toggleUploadSheet(true) },
                    onOpenEqualizer = { viewModel.toggleEqualizer(true) }
                )
            }
        }
    }

    // Full Screen Player Sheet
    if (showFullPlayer) {
        currentTrack?.let { track ->
            FullPlayerSheet(
                track = track,
                isPlaying = isPlaying,
                playbackPositionMs = playbackPositionMs,
                durationMs = durationMs,
                parsedLyrics = parsedLyrics,
                activeLyricIndex = activeLyricIndex,
                repeatMode = repeatMode,
                isShuffle = isShuffle,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.playNextTrack() },
                onPrevClick = { viewModel.playPreviousTrack() },
                onSeekTo = { pos -> viewModel.seekToPosition(pos) },
                onLikeToggle = { viewModel.toggleLikeTrack(track) },
                onOfflineToggle = { viewModel.toggleOfflineDownload(track) },
                onRepeatToggle = { viewModel.toggleRepeatMode() },
                onShuffleToggle = { viewModel.toggleShuffle() },
                onOpenEqualizer = { viewModel.toggleEqualizer(true) },
                onDismiss = { viewModel.toggleFullPlayer(false) }
            )
        }
    }

    // In-App Built-in Equalizer Sheet
    if (showEqualizer) {
        EqualizerSheet(
            eqEnabled = eqEnabled,
            preset = eqPreset,
            band60Hz = band60Hz,
            band230Hz = band230Hz,
            band910Hz = band910Hz,
            band3600Hz = band3600Hz,
            band14000Hz = band14000Hz,
            bassBoost = bassBoost,
            virtualizer = virtualizer,
            isPlaying = isPlaying,
            onEqEnabledChange = { viewModel.setEqEnabled(it) },
            onPresetSelect = { viewModel.setEqPreset(it) },
            onBand60HzChange = { viewModel.setBand60Hz(it) },
            onBand230HzChange = { viewModel.setBand230Hz(it) },
            onBand910HzChange = { viewModel.setBand910Hz(it) },
            onBand3600HzChange = { viewModel.setBand3600Hz(it) },
            onBand14000HzChange = { viewModel.setBand14000Hz(it) },
            onBassBoostChange = { viewModel.setBassBoost(it) },
            onVirtualizerChange = { viewModel.setVirtualizer(it) },
            onDismiss = { viewModel.toggleEqualizer(false) }
        )
    }

    // Upload MP3/Video Sheet
    if (showUploadSheet) {
        UploadTrackSheet(
            isUploading = isUploading,
            uploadProgress = uploadProgress,
            uploadStatusMessage = uploadStatusMessage,
            onStartUpload = { title, artist, album, genre, lyrics, fileType, uploader ->
                viewModel.startUploadProcess(title, artist, album, genre, lyrics, fileType, uploader)
            },
            onDismiss = { viewModel.toggleUploadSheet(false) }
        )
    }
}
