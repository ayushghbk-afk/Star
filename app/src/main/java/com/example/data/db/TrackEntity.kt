package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val audioUrl: String,
    val coverUrl: String,
    val genre: String,
    val isLiked: Boolean = false,
    val isOfflineDownloaded: Boolean = false,
    val isUserUploaded: Boolean = false,
    val lyricsSyncJson: String, // Timed lyrics: timestampInMs|lyricText line by line
    val likesCount: Int = 120,
    val playCount: Int = 1450,
    val uploaderName: String = "StreamSync Server",
    val addedTimestamp: Long = System.currentTimeMillis()
)
