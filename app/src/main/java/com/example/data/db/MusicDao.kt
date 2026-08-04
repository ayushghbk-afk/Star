package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY addedTimestamp DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isOfflineDownloaded = 1 ORDER BY addedTimestamp DESC")
    fun getOfflineTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isUserUploaded = 1 ORDER BY addedTimestamp DESC")
    fun getUserUploadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY addedTimestamp DESC")
    fun getLikedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateLiked(id: Long, isLiked: Boolean)

    @Query("UPDATE tracks SET isOfflineDownloaded = :isOffline WHERE id = :id")
    suspend fun updateOfflineState(id: Long, isOffline: Boolean)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.orderIndex ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long

    @Query("UPDATE community_posts SET likesCount = likesCount + (CASE WHEN :isLiked THEN 1 ELSE -1 END), isLikedByMe = :isLiked WHERE id = :postId")
    suspend fun togglePostLike(postId: Long, isLiked: Boolean)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(prefs: UserPreferencesEntity)
}
