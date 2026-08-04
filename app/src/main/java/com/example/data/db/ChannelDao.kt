package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY subscriberCount DESC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isSubscribed = 1")
    fun getSubscribedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE channelId = :channelId LIMIT 1")
    suspend fun getChannelByChannelId(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE ownerUserId = :userId LIMIT 1")
    suspend fun getChannelByOwnerUserId(userId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isSubscribed = :isSubscribed, subscriberCount = subscriberCount + :delta WHERE id = :id")
    suspend fun updateSubscription(id: Long, isSubscribed: Boolean, delta: Int)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
}
