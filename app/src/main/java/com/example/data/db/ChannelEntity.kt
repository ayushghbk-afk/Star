package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: String,
    val name: String,
    val handle: String,
    val bio: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscriberCount: Int = 0,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val ownerUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
