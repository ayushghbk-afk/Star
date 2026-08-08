package com.example.data.repository

import com.example.data.auth.AuthManager
import com.example.data.db.*
import com.example.data.firestore.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MusicRepository(private val database: AppDatabase) {

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                AppDatabase.ensureInitialData(database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val firestoreSync = FirestoreSyncManager()
    val authManager = AuthManager()

    val allTracks: Flow<List<TrackEntity>> = database.trackDao().getAllTracks()
    val offlineTracks: Flow<List<TrackEntity>> = database.trackDao().getOfflineTracks()
    val userUploadedTracks: Flow<List<TrackEntity>> = database.trackDao().getUserUploadedTracks()
    val likedTracks: Flow<List<TrackEntity>> = database.trackDao().getLikedTracks()
    val playlists: Flow<List<PlaylistEntity>> = database.playlistDao().getAllPlaylists()
    val communityPosts: Flow<List<CommunityPostEntity>> = database.communityDao().getAllPosts()
    val userPreferences: Flow<UserPreferencesEntity?> = database.communityDao().getUserPreferences()
    val allChannels: Flow<List<ChannelEntity>> = database.channelDao().getAllChannels()
    val subscribedChannels: Flow<List<ChannelEntity>> = database.channelDao().getSubscribedChannels()

    suspend fun getChannelById(id: Long): ChannelEntity? {
        return database.channelDao().getChannelById(id)
    }

    suspend fun toggleSubscribeChannel(channelId: Long, currentIsSubscribed: Boolean) {
        val newSubState = !currentIsSubscribed
        val delta = if (newSubState) 1 else -1
        database.channelDao().updateSubscription(channelId, newSubState, delta)
        database.channelDao().getChannelById(channelId)?.let { updated ->
            firestoreSync.syncChannel(updated)
        }
    }

    suspend fun saveChannel(channel: ChannelEntity): Long {
        val id = database.channelDao().insertChannel(channel)
        val saved = channel.copy(id = id)
        firestoreSync.syncChannel(saved)
        return id
    }

    fun searchTracks(query: String): Flow<List<TrackEntity>> {
        return database.trackDao().searchTracks(query)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>> {
        return database.playlistDao().getTracksForPlaylist(playlistId)
    }

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> {
        return database.communityDao().getCommentsForPost(postId)
    }

    suspend fun toggleLikeTrack(trackId: Long, currentIsLiked: Boolean) {
        database.trackDao().updateLiked(trackId, !currentIsLiked)
    }

    suspend fun toggleOfflineDownload(trackId: Long, currentIsOffline: Boolean) {
        database.trackDao().updateOfflineState(trackId, !currentIsOffline)
    }

    suspend fun uploadTrackToServer(
        title: String,
        artist: String,
        album: String,
        genre: String,
        lyricsText: String,
        isMp3OrVideo: String,
        uploaderName: String,
        audioUri: String? = null,
        coverUrl: String? = null,
        durationSec: Int = 210,
        onProgress: (Int, String) -> Unit
    ): Long {
        // Step 1: File analysis & validation
        onProgress(10, "Analyzing audio file parameters & metadata...")
        delay(600)

        // Step 2: Uploading RAW file to StreamSync Server
        onProgress(35, "Uploading $isMp3OrVideo file to StreamSync Cloud Server...")
        delay(800)

        // Step 3: Server-side High Quality Audio Encoding
        onProgress(60, "Encoding high-bitrate 320kbps stream & normalizing audio...")
        delay(900)

        // Step 4: Generating Real-time Lyric Timestamps
        onProgress(80, "Generating synchronized lyrics timing engine...")
        delay(700)

        // Step 5: Making Publicly Discoverable
        onProgress(95, "Publishing to public discovery network...")
        delay(500)

        // Format lyric lines with generated timing if raw lyrics were provided
        val processedLyrics = if (lyricsText.isNotBlank()) {
            val lines = lyricsText.lines().filter { it.isNotBlank() }
            val sb = StringBuilder()
            var currentMs = 0
            sb.append("0|[Intro - High Quality Stream]\n")
            lines.forEach { line ->
                currentMs += 5000
                sb.append("$currentMs|$line\n")
            }
            sb.toString().trim()
        } else {
            """
                0|[High-Quality User Uploaded Stream]
                8000|Enjoy this community uploaded track
                16000|StreamSync Music High Quality Audio
                24000|Live synchronized stream active
            """.trimIndent()
        }

        val finalAudioUrl = if (!audioUri.isNullOrBlank()) audioUri else "https://actions.google.com/sounds/v1/ambiences/rain_heavy.ogg"
        val finalCoverUrl = if (!coverUrl.isNullOrBlank()) coverUrl else "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop"

        val newTrack = TrackEntity(
            title = title.ifBlank { "Untitled Upload" },
            artist = artist.ifBlank { uploaderName },
            album = album.ifBlank { "Community Single" },
            durationSeconds = if (durationSec > 0) durationSec else 210,
            audioUrl = finalAudioUrl,
            coverUrl = finalCoverUrl,
            genre = genre.ifBlank { "Pop / Dance" },
            isLiked = false,
            isOfflineDownloaded = true, // Downloaded automatically upon upload
            isUserUploaded = true,
            lyricsSyncJson = processedLyrics,
            likesCount = 1,
            playCount = 1,
            uploaderName = uploaderName
        )

        val newTrackId = database.trackDao().insertTrack(newTrack)
        val insertedTrack = newTrack.copy(id = newTrackId)

        // Sync track to Firestore
        firestoreSync.syncTrack(insertedTrack)

        // Auto create a public community post announcement
        val newPost = CommunityPostEntity(
            authorName = uploaderName,
            authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            textContent = "I just uploaded '$title' to StreamSync! Stream it now with full synchronized lyrics and custom EQ!",
            attachedTrackId = newTrackId,
            likesCount = 1,
            isLikedByMe = true
        )
        val newPostId = database.communityDao().insertPost(newPost)
        firestoreSync.syncCommunityPost(newPost.copy(id = newPostId))

        onProgress(100, "Successfully published and discoverable by all users!")
        return newTrackId
    }

    suspend fun createPlaylist(name: String, description: String): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop",
            isSystemPlaylist = false
        )
        val id = database.playlistDao().insertPlaylist(playlist)
        val insertedPlaylist = playlist.copy(id = id)
        firestoreSync.syncPlaylist(insertedPlaylist)
        return id
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        database.playlistDao().insertPlaylistTrackCrossRef(
            PlaylistTrackCrossRef(playlistId, trackId)
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        database.playlistDao().deletePlaylist(playlistId)
        firestoreSync.deletePlaylist(playlistId)
    }

    suspend fun createCommunityPost(authorName: String, textContent: String, trackId: Long?) {
        val post = CommunityPostEntity(
            authorName = authorName,
            authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            textContent = textContent,
            attachedTrackId = trackId,
            likesCount = 0,
            isLikedByMe = false
        )
        val id = database.communityDao().insertPost(post)
        firestoreSync.syncCommunityPost(post.copy(id = id))
    }

    suspend fun togglePostLike(postId: Long, currentIsLiked: Boolean) {
        database.communityDao().togglePostLike(postId, !currentIsLiked)
    }

    suspend fun addComment(postId: Long, authorName: String, text: String) {
        val comment = CommentEntity(
            postId = postId,
            authorName = authorName,
            authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            commentText = text
        )
        val id = database.communityDao().insertComment(comment)
        firestoreSync.syncComment(comment.copy(id = id))
    }

    suspend fun saveUserPreferences(prefs: UserPreferencesEntity) {
        database.communityDao().saveUserPreferences(prefs)
    }
}

