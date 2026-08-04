package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.MusicRepository
import com.example.ui.model.LyricLine
import com.example.ui.model.LyricsParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class RepeatMode { OFF, ALL, ONE }

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(AppDatabase.getDatabase(application))

    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineTracks: StateFlow<List<TrackEntity>> = repository.offlineTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userUploadedTracks: StateFlow<List<TrackEntity>> = repository.userUploadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedTracks: StateFlow<List<TrackEntity>> = repository.likedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferencesEntity?> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUser = repository.authManager.currentUser

    val allChannels: StateFlow<List<ChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscribedChannels: StateFlow<List<ChannelEntity>> = repository.subscribedChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChannel = MutableStateFlow<ChannelEntity?>(null)
    val selectedChannel: StateFlow<ChannelEntity?> = _selectedChannel.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showChannelSheet = MutableStateFlow(false)
    val showChannelSheet: StateFlow<Boolean> = _showChannelSheet.asStateFlow()

    private val _showCreateChannelSheet = MutableStateFlow(false)
    val showCreateChannelSheet: StateFlow<Boolean> = _showCreateChannelSheet.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<TrackEntity>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allTracks
            else repository.searchTracks(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected player track & queue
    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _parsedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val parsedLyrics: StateFlow<List<LyricLine>> = _parsedLyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(0)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    // UI Sheets visibility
    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer: StateFlow<Boolean> = _showFullPlayer.asStateFlow()

    private val _showEqualizer = MutableStateFlow(false)
    val showEqualizer: StateFlow<Boolean> = _showEqualizer.asStateFlow()

    private val _showUploadSheet = MutableStateFlow(false)
    val showUploadSheet: StateFlow<Boolean> = _showUploadSheet.asStateFlow()

    // Upload progress
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    private val _uploadStatusMessage = MutableStateFlow("")
    val uploadStatusMessage: StateFlow<String> = _uploadStatusMessage.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Equalizer sliders
    private val _eqEnabled = MutableStateFlow(true)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqPreset = MutableStateFlow("Bass Booster")
    val eqPreset: StateFlow<String> = _eqPreset.asStateFlow()

    private val _band60Hz = MutableStateFlow(5f)
    val band60Hz: StateFlow<Float> = _band60Hz.asStateFlow()

    private val _band230Hz = MutableStateFlow(3f)
    val band230Hz: StateFlow<Float> = _band230Hz.asStateFlow()

    private val _band910Hz = MutableStateFlow(0f)
    val band910Hz: StateFlow<Float> = _band910Hz.asStateFlow()

    private val _band3600Hz = MutableStateFlow(2f)
    val band3600Hz: StateFlow<Float> = _band3600Hz.asStateFlow()

    private val _band14000Hz = MutableStateFlow(4f)
    val band14000Hz: StateFlow<Float> = _band14000Hz.asStateFlow()

    private val _bassBoost = MutableStateFlow(0.8f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(0.6f)
    val virtualizer: StateFlow<Float> = _virtualizer.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Observe tracks to initialize current track if null
        viewModelScope.launch {
            allTracks.collect { list ->
                if (_currentTrack.value == null && list.isNotEmpty()) {
                    setTrackQueue(list, 0, autoPlay = false)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTrackQueue(tracks: List<TrackEntity>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (tracks.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, tracks.lastIndex)
        _queue.value = tracks
        _currentQueueIndex.value = validIndex
        val selected = tracks[validIndex]
        loadTrack(selected, autoPlay)
    }

    fun playTrackNow(track: TrackEntity) {
        val currentList = _queue.value.ifEmpty { listOf(track) }
        val index = currentList.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            _currentQueueIndex.value = index
            loadTrack(track, autoPlay = true)
        } else {
            val newList = listOf(track) + currentList
            _queue.value = newList
            _currentQueueIndex.value = 0
            loadTrack(track, autoPlay = true)
        }
    }

    private fun loadTrack(track: TrackEntity, autoPlay: Boolean) {
        _currentTrack.value = track
        _durationMs.value = (track.durationSeconds * 1000L).coerceAtLeast(10000L)
        _playbackPositionMs.value = 0L

        // Parse synchronized lyrics
        val lyrics = LyricsParser.parseLyrics(track.lyricsSyncJson)
        _parsedLyrics.value = lyrics
        _activeLyricIndex.value = 0

        if (autoPlay) {
            startPlaybackTimer()
        } else {
            _isPlaying.value = false
            stopPlaybackTimer()
        }
    }

    fun togglePlayPause() {
        if (_currentTrack.value == null && allTracks.value.isNotEmpty()) {
            setTrackQueue(allTracks.value, 0, autoPlay = true)
            return
        }
        if (_isPlaying.value) {
            _isPlaying.value = false
            stopPlaybackTimer()
        } else {
            _isPlaying.value = true
            startPlaybackTimer()
        }
    }

    fun playNextTrack() {
        val queueList = _queue.value
        if (queueList.isEmpty()) return

        var nextIndex = _currentQueueIndex.value + 1
        if (nextIndex >= queueList.size) {
            nextIndex = 0
        }
        _currentQueueIndex.value = nextIndex
        loadTrack(queueList[nextIndex], autoPlay = true)
    }

    fun playPreviousTrack() {
        val queueList = _queue.value
        if (queueList.isEmpty()) return

        var prevIndex = _currentQueueIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = queueList.lastIndex
        }
        _currentQueueIndex.value = prevIndex
        loadTrack(queueList[prevIndex], autoPlay = true)
    }

    fun seekToPosition(positionMs: Long) {
        val maxDur = _durationMs.value
        _playbackPositionMs.value = positionMs.coerceIn(0L, maxDur)
        updateLyricIndex(positionMs)
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        _isPlaying.value = true
        playbackJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(200)
                val current = _playbackPositionMs.value + 200
                val total = _durationMs.value
                if (current >= total) {
                    when (_repeatMode.value) {
                        RepeatMode.ONE -> {
                            _playbackPositionMs.value = 0L
                            updateLyricIndex(0L)
                        }
                        RepeatMode.ALL -> playNextTrack()
                        RepeatMode.OFF -> {
                            if (_currentQueueIndex.value < _queue.value.lastIndex) {
                                playNextTrack()
                            } else {
                                _playbackPositionMs.value = total
                                _isPlaying.value = false
                                stopPlaybackTimer()
                            }
                        }
                    }
                } else {
                    _playbackPositionMs.value = current
                    updateLyricIndex(current)
                }
            }
        }
    }

    private fun stopPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = null
    }

    private fun updateLyricIndex(currentPositionMs: Long) {
        val lyrics = _parsedLyrics.value
        if (lyrics.isEmpty()) return

        var idx = 0
        for (i in lyrics.indices) {
            if (lyrics[i].timestampMs <= currentPositionMs) {
                idx = i
            } else {
                break
            }
        }
        _activeLyricIndex.value = idx
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleFullPlayer(show: Boolean) {
        _showFullPlayer.value = show
    }

    fun toggleEqualizer(show: Boolean) {
        _showEqualizer.value = show
    }

    fun toggleUploadSheet(show: Boolean) {
        _showUploadSheet.value = show
    }

    fun toggleLikeTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleLikeTrack(track.id, track.isLiked)
            // Update currently loaded track if same
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(isLiked = !track.isLiked)
            }
        }
    }

    fun toggleOfflineDownload(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleOfflineDownload(track.id, track.isOfflineDownloaded)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(isOfflineDownloaded = !track.isOfflineDownloaded)
            }
        }
    }

    fun togglePremiumSubscriber() {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferencesEntity()
            val updated = current.copy(isPremium = !current.isPremium)
            repository.saveUserPreferences(updated)
        }
    }

    // Server Upload Processing
    fun startUploadProcess(
        title: String,
        artist: String,
        album: String,
        genre: String,
        lyricsText: String,
        fileType: String,
        uploaderName: String
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0
            _uploadStatusMessage.value = "Starting upload sequence..."

            val newId = repository.uploadTrackToServer(
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                lyricsText = lyricsText,
                isMp3OrVideo = fileType,
                uploaderName = uploaderName,
                onProgress = { progress, msg ->
                    _uploadProgress.value = progress
                    _uploadStatusMessage.value = msg
                }
            )

            _isUploading.value = false
            _showUploadSheet.value = false

            // Auto load and play newly uploaded & processed track!
            val uploadedTrack = repository.allTracks.first().firstOrNull { it.id == newId }
            uploadedTrack?.let {
                playTrackNow(it)
                _showFullPlayer.value = true
            }
        }
    }

    // Equalizer controls
    fun setEqPreset(preset: String) {
        _eqPreset.value = preset
        when (preset) {
            "Bass Booster" -> {
                _band60Hz.value = 8f
                _band230Hz.value = 5f
                _band910Hz.value = 1f
                _band3600Hz.value = 2f
                _band14000Hz.value = 3f
                _bassBoost.value = 0.9f
            }
            "Flat" -> {
                _band60Hz.value = 0f
                _band230Hz.value = 0f
                _band910Hz.value = 0f
                _band3600Hz.value = 0f
                _band14000Hz.value = 0f
                _bassBoost.value = 0f
            }
            "Vocal Booster" -> {
                _band60Hz.value = -2f
                _band230Hz.value = 1f
                _band910Hz.value = 6f
                _band3600Hz.value = 7f
                _band14000Hz.value = 3f
                _bassBoost.value = 0.2f
            }
            "Rock" -> {
                _band60Hz.value = 6f
                _band230Hz.value = 4f
                _band910Hz.value = -1f
                _band3600Hz.value = 5f
                _band14000Hz.value = 7f
                _bassBoost.value = 0.6f
            }
            "Electronic" -> {
                _band60Hz.value = 7f
                _band230Hz.value = 6f
                _band910Hz.value = 1f
                _band3600Hz.value = 4f
                _band14000Hz.value = 6f
                _bassBoost.value = 0.8f
            }
            "Hip-Hop" -> {
                _band60Hz.value = 9f
                _band230Hz.value = 7f
                _band910Hz.value = 2f
                _band3600Hz.value = 1f
                _band14000Hz.value = 4f
                _bassBoost.value = 1.0f
            }
        }
    }

    fun setEqEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
    }

    fun setBand60Hz(value: Float) { _band60Hz.value = value; _eqPreset.value = "Custom" }
    fun setBand230Hz(value: Float) { _band230Hz.value = value; _eqPreset.value = "Custom" }
    fun setBand910Hz(value: Float) { _band910Hz.value = value; _eqPreset.value = "Custom" }
    fun setBand3600Hz(value: Float) { _band3600Hz.value = value; _eqPreset.value = "Custom" }
    fun setBand14000Hz(value: Float) { _band14000Hz.value = value; _eqPreset.value = "Custom" }
    fun setBassBoost(value: Float) { _bassBoost.value = value; _eqPreset.value = "Custom" }
    fun setVirtualizer(value: Float) { _virtualizer.value = value; _eqPreset.value = "Custom" }

    // Playlist CRUD
    fun createPlaylist(name: String, desc: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, desc)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    // Community Board
    fun createPost(author: String, text: String, trackId: Long?) {
        viewModelScope.launch {
            repository.createCommunityPost(author, text, trackId)
        }
    }

    fun togglePostLike(post: CommunityPostEntity) {
        viewModelScope.launch {
            repository.togglePostLike(post.id, post.isLikedByMe)
        }
    }

    fun addComment(postId: Long, author: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, author, text)
        }
    }

    // Auth & Channel Actions
    fun setShowAuthDialog(show: Boolean) {
        _showAuthDialog.value = show
    }

    fun setShowChannelSheet(show: Boolean) {
        _showChannelSheet.value = show
    }

    fun setShowCreateChannelSheet(show: Boolean) {
        _showCreateChannelSheet.value = show
    }

    fun openChannel(channel: ChannelEntity) {
        _selectedChannel.value = channel
        _showChannelSheet.value = true
    }

    fun toggleSubscribe(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleSubscribeChannel(channel.id, channel.isSubscribed)
            // Update selectedChannel if open
            if (_selectedChannel.value?.id == channel.id) {
                val newSub = !channel.isSubscribed
                val delta = if (newSub) 1 else -1
                _selectedChannel.value = channel.copy(
                    isSubscribed = newSub,
                    subscriberCount = (channel.subscriberCount + delta).coerceAtLeast(0)
                )
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        repository.authManager.signInWithEmail(email, pass) { success, msg ->
            if (success) _showAuthDialog.value = false
            onResult(success, msg)
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, onResult: (Boolean, String?) -> Unit) {
        repository.authManager.signUpWithEmail(email, pass, name) { success, msg ->
            if (success) {
                _showAuthDialog.value = false
                // Auto create channel for new account
                createChannel(
                    name = name.ifBlank { "Channel of $email" },
                    handle = "@${name.lowercase().replace(" ", "")}",
                    bio = "Official music channel on StreamSync."
                )
            }
            onResult(success, msg)
        }
    }

    fun signOut() {
        repository.authManager.signOut()
    }

    fun createChannel(name: String, handle: String, bio: String) {
        val user = currentUser.value
        val channel = ChannelEntity(
            channelId = "channel_${System.currentTimeMillis()}",
            name = name,
            handle = if (handle.startsWith("@")) handle else "@$handle",
            bio = bio,
            avatarUrl = user?.photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            bannerUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop",
            subscriberCount = 1,
            isSubscribed = false,
            isVerified = false,
            ownerUserId = user?.uid ?: "guest_user_101"
        )
        viewModelScope.launch {
            val newId = repository.saveChannel(channel)
            val savedChannel = channel.copy(id = newId)
            _selectedChannel.value = savedChannel
            _showCreateChannelSheet.value = false
            _showChannelSheet.value = true
        }
    }
}
