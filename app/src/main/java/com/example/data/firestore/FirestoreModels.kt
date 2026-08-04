package com.example.data.firestore

import com.example.data.db.CommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity

/**
 * Data model for storing user tracks in Firebase Firestore.
 */
data class FirestoreTrack(
    val id: String = "",
    val localId: Long = 0,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationSeconds: Int = 0,
    val audioUrl: String = "",
    val coverUrl: String = "",
    val genre: String = "",
    val isLiked: Boolean = false,
    val isOfflineDownloaded: Boolean = false,
    val isUserUploaded: Boolean = false,
    val lyricsSyncJson: String = "",
    val likesCount: Int = 0,
    val playCount: Int = 0,
    val uploaderName: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    fun toTrackEntity(): TrackEntity = TrackEntity(
        id = if (localId != 0L) localId else 0L,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        genre = genre,
        isLiked = isLiked,
        isOfflineDownloaded = isOfflineDownloaded,
        isUserUploaded = isUserUploaded,
        lyricsSyncJson = lyricsSyncJson,
        likesCount = likesCount,
        playCount = playCount,
        uploaderName = uploaderName,
        addedTimestamp = addedTimestamp
    )

    companion object {
        fun fromTrackEntity(entity: TrackEntity, docId: String = ""): FirestoreTrack = FirestoreTrack(
            id = docId.ifBlank { entity.id.toString() },
            localId = entity.id,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            durationSeconds = entity.durationSeconds,
            audioUrl = entity.audioUrl,
            coverUrl = entity.coverUrl,
            genre = entity.genre,
            isLiked = entity.isLiked,
            isOfflineDownloaded = entity.isOfflineDownloaded,
            isUserUploaded = entity.isUserUploaded,
            lyricsSyncJson = entity.lyricsSyncJson,
            likesCount = entity.likesCount,
            playCount = entity.playCount,
            uploaderName = entity.uploaderName,
            addedTimestamp = entity.addedTimestamp
        )
    }
}

/**
 * Data model for storing user playlists in Firebase Firestore.
 */
data class FirestorePlaylist(
    val id: String = "",
    val localId: Long = 0,
    val name: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val isSystemPlaylist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val trackIds: List<Long> = emptyList()
) {
    fun toPlaylistEntity(): PlaylistEntity = PlaylistEntity(
        id = if (localId != 0L) localId else 0L,
        name = name,
        description = description,
        coverUrl = coverUrl,
        isSystemPlaylist = isSystemPlaylist,
        createdAt = createdAt
    )

    companion object {
        fun fromPlaylistEntity(entity: PlaylistEntity, trackIds: List<Long> = emptyList(), docId: String = ""): FirestorePlaylist =
            FirestorePlaylist(
                id = docId.ifBlank { entity.id.toString() },
                localId = entity.id,
                name = entity.name,
                description = entity.description,
                coverUrl = entity.coverUrl,
                isSystemPlaylist = entity.isSystemPlaylist,
                createdAt = entity.createdAt,
                trackIds = trackIds
            )
    }
}

/**
 * Data model for storing community board posts in Firebase Firestore.
 */
data class FirestoreCommunityPost(
    val id: String = "",
    val localId: Long = 0,
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val textContent: String = "",
    val attachedTrackId: Long? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toCommunityPostEntity(): CommunityPostEntity = CommunityPostEntity(
        id = if (localId != 0L) localId else 0L,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        textContent = textContent,
        attachedTrackId = attachedTrackId,
        likesCount = likesCount,
        isLikedByMe = isLikedByMe,
        createdAt = createdAt
    )

    companion object {
        fun fromCommunityPostEntity(entity: CommunityPostEntity, docId: String = ""): FirestoreCommunityPost =
            FirestoreCommunityPost(
                id = docId.ifBlank { entity.id.toString() },
                localId = entity.id,
                authorName = entity.authorName,
                authorAvatarUrl = entity.authorAvatarUrl,
                textContent = entity.textContent,
                attachedTrackId = entity.attachedTrackId,
                likesCount = entity.likesCount,
                isLikedByMe = entity.isLikedByMe,
                createdAt = entity.createdAt
            )
    }
}

/**
 * Data model for storing community comments in Firebase Firestore.
 */
data class FirestoreComment(
    val id: String = "",
    val localId: Long = 0,
    val postId: Long = 0,
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toCommentEntity(): CommentEntity = CommentEntity(
        id = if (localId != 0L) localId else 0L,
        postId = postId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        commentText = commentText,
        timestamp = timestamp
    )

    companion object {
        fun fromCommentEntity(entity: CommentEntity, docId: String = ""): FirestoreComment =
            FirestoreComment(
                id = docId.ifBlank { entity.id.toString() },
                localId = entity.id,
                postId = entity.postId,
                authorName = entity.authorName,
                authorAvatarUrl = entity.authorAvatarUrl,
                commentText = entity.commentText,
                timestamp = entity.timestamp
            )
    }
}

/**
 * Data model for storing Channel / Profile details in Firebase Firestore.
 */
data class FirestoreChannel(
    val id: String = "",
    val localId: Long = 0,
    val channelId: String = "",
    val name: String = "",
    val handle: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val subscriberCount: Int = 0,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val ownerUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toChannelEntity(): com.example.data.db.ChannelEntity = com.example.data.db.ChannelEntity(
        id = if (localId != 0L) localId else 0L,
        channelId = channelId,
        name = name,
        handle = handle,
        bio = bio,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        subscriberCount = subscriberCount,
        isSubscribed = isSubscribed,
        isVerified = isVerified,
        ownerUserId = ownerUserId,
        createdAt = createdAt
    )

    companion object {
        fun fromChannelEntity(entity: com.example.data.db.ChannelEntity, docId: String = ""): FirestoreChannel =
            FirestoreChannel(
                id = docId.ifBlank { entity.id.toString() },
                localId = entity.id,
                channelId = entity.channelId,
                name = entity.name,
                handle = entity.handle,
                bio = entity.bio,
                avatarUrl = entity.avatarUrl,
                bannerUrl = entity.bannerUrl,
                subscriberCount = entity.subscriberCount,
                isSubscribed = entity.isSubscribed,
                isVerified = entity.isVerified,
                ownerUserId = entity.ownerUserId,
                createdAt = entity.createdAt
            )
    }
}
