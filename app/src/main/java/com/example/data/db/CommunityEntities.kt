package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val authorAvatarUrl: String,
    val textContent: String,
    val attachedTrackId: Long? = null,
    val likesCount: Int = 12,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarUrl: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val isPremium: Boolean = true,
    val eqEnabled: Boolean = true,
    val eqPresetName: String = "Bass Booster",
    val band60Hz: Float = 4f,
    val band230Hz: Float = 2f,
    val band910Hz: Float = 0f,
    val band3600Hz: Float = 3f,
    val band14000Hz: Float = 5f,
    val bassBoost: Float = 0.7f,
    val virtualizer: Float = 0.5f
)
