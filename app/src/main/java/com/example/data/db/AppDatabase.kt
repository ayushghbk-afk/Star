package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        CommunityPostEntity::class,
        CommentEntity::class,
        UserPreferencesEntity::class,
        ChannelEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun communityDao(): CommunityDao
    abstract fun channelDao(): ChannelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "streamsync_music.db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val trackDao = database.trackDao()
            val playlistDao = database.playlistDao()
            val communityDao = database.communityDao()
            val channelDao = database.channelDao()

            // Default User Preferences
            communityDao.saveUserPreferences(UserPreferencesEntity())

            // Sample Channels
            val sampleChannels = listOf(
                ChannelEntity(
                    id = 1,
                    channelId = "channel_neon_horizon",
                    name = "Neon Horizon Official",
                    handle = "@neonhorizon",
                    bio = "Electronic Synthwave producer & visual artist. Live streams every Friday night!",
                    avatarUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop",
                    bannerUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop",
                    subscriberCount = 142000,
                    isSubscribed = true,
                    isVerified = true,
                    ownerUserId = "artist_101"
                ),
                ChannelEntity(
                    id = 2,
                    channelId = "channel_aura_luna",
                    name = "Aura & Luna Studios",
                    handle = "@auraluna_ambient",
                    bio = "Chillout, Ambient Lo-Fi & Deep Focus Music for relaxation and study.",
                    avatarUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop",
                    bannerUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&auto=format&fit=crop",
                    subscriberCount = 98500,
                    isSubscribed = false,
                    isVerified = true,
                    ownerUserId = "artist_102"
                ),
                ChannelEntity(
                    id = 3,
                    channelId = "channel_my_creator_channel",
                    name = "Creator Studio Channel",
                    handle = "@creator_user",
                    bio = "Official Channel of Creator User on StreamSync. Uploading new hits daily!",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                    bannerUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&auto=format&fit=crop",
                    subscriberCount = 1250,
                    isSubscribed = false,
                    isVerified = false,
                    ownerUserId = "guest_user_101"
                )
            )
            channelDao.insertChannels(sampleChannels)

            // Sample Tracks with synchronized lyrics (timestampInMs|lyricText)
            val sampleTracks = listOf(
                TrackEntity(
                    id = 101,
                    title = "Midnight Echoes",
                    artist = "Neon Horizon",
                    album = "Synthwave Pulse 2026",
                    durationSeconds = 210,
                    audioUrl = "https://actions.google.com/sounds/v1/ambiences/rain_heavy.ogg",
                    coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop",
                    genre = "Synthwave",
                    isLiked = true,
                    isOfflineDownloaded = true,
                    isUserUploaded = false,
                    likesCount = 2420,
                    playCount = 18900,
                    lyricsSyncJson = """
                        0|[Instrumental Intro - Pulsing Synth]
                        8000|Walking through the city lights at midnight
                        15000|Fading shadows on the neon boulevard
                        22000|Every beat feels like an echo in my heart
                        30000|Electric dreams beneath the starlit sky
                        38000|[Chorus]
                        42000|Midnight echoes calling out your name
                        50000|Lost in the rhythm of the endless rain
                        58000|Midnight echoes, nothing stays the same
                        66000|[Guitar Synth Solo]
                        80000|Digital horizon glowing in the dark
                        88000|We keep riding on the cyber arc
                        96000|Midnight echoes fading into dawn
                        110000|[Fade out]
                    """.trimIndent()
                ),
                TrackEntity(
                    id = 102,
                    title = "Celestial Waves",
                    artist = "Aura & Luna",
                    album = "Deep Focus Cosmos",
                    durationSeconds = 185,
                    audioUrl = "https://actions.google.com/sounds/v1/science_fiction/space_engine_large.ogg",
                    coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop",
                    genre = "Chillout / Lo-Fi",
                    isLiked = false,
                    isOfflineDownloaded = true,
                    isUserUploaded = false,
                    likesCount = 890,
                    playCount = 6400,
                    lyricsSyncJson = """
                        0|[Soft Ambient Piano Pads]
                        10000|Floating through the outer space
                        18000|Silent beauty in a timeless place
                        26000|Stars align in harmony
                        34000|Celestial waves encompass me
                        44000|[Soft Beat Drop]
                        54000|Breathe in the calm, release the sound
                        64000|No gravity, no solid ground
                        76000|Celestial waves eternal flow
                        90000|[Ambient Pad Outro]
                    """.trimIndent()
                ),
                TrackEntity(
                    id = 103,
                    title = "Electric Summer",
                    artist = "The Solar Collective",
                    album = "Sunburn Beats",
                    durationSeconds = 198,
                    audioUrl = "https://actions.google.com/sounds/v1/water/ocean_waves.ogg",
                    coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop",
                    genre = "Dance / Pop",
                    isLiked = true,
                    isOfflineDownloaded = false,
                    isUserUploaded = false,
                    likesCount = 3100,
                    playCount = 29000,
                    lyricsSyncJson = """
                        0|[Uplifting Guitar & Bassline]
                        6000|Golden sunshine in the afternoon
                        12000|Dancing underneath a summer moon
                        18000|Feel the breeze along the ocean shore
                        25000|Give me energy and give me more!
                        32000|[Drop - Vibrant Dance Beat]
                        40000|This is our electric summer night!
                        48000|Shining through the golden light
                        56000|Electric summer, hold on tight!
                        70000|[Bass Groove Solo]
                        85000|Never let the summer fade away
                        95000|Living for the magic of today
                    """.trimIndent()
                ),
                TrackEntity(
                    id = 104,
                    title = "Urban Gravity",
                    artist = "K-Beat Machine",
                    album = "Metropolis Vol. 1",
                    durationSeconds = 160,
                    audioUrl = "https://actions.google.com/sounds/v1/transportation/subway_train.ogg",
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop",
                    genre = "Hip-Hop / Instrumental",
                    isLiked = false,
                    isOfflineDownloaded = false,
                    isUserUploaded = true,
                    uploaderName = "DJ Alex (User Upload)",
                    likesCount = 540,
                    playCount = 3100,
                    lyricsSyncJson = """
                        0|[Heavy Sub Bass & Vinyl Crackle]
                        8000|Subway lines and concrete streets
                        16000|Rhythm created by the urban beats
                        24000|Gravity pulling us together
                        32000|Hip-Hop spirit lives forever
                        42000|[Scratches & Bass Drop]
                        52000|Metropolis pulsing in the dark
                        62000|Every street light ignites a spark
                    """.trimIndent()
                )
            )

            trackDao.insertTracks(sampleTracks)

            // Seed System Playlists
            val p1Id = playlistDao.insertPlaylist(
                PlaylistEntity(
                    id = 1,
                    name = "Trending Hits 2026",
                    description = "Top public streams and uploaded tracks trending on StreamSync",
                    coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
                    isSystemPlaylist = true
                )
            )

            val p2Id = playlistDao.insertPlaylist(
                PlaylistEntity(
                    id = 2,
                    name = "Community Upload Spotlight",
                    description = "Freshly uploaded tracks processed and shared by our music creator community",
                    coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop",
                    isSystemPlaylist = true
                )
            )

            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(p1Id, 101, 1))
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(p1Id, 103, 2))
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(p2Id, 104, 1))

            // Seed Community Board Posts
            val post1Id = communityDao.insertPost(
                CommunityPostEntity(
                    id = 1,
                    authorName = "Sophia Resonance",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                    textContent = "Just finished listening to 'Midnight Echoes' with Bass Booster EQ on! The real-time lyrics sync is incredible on this track! 🎶🎧 Check it out!",
                    attachedTrackId = 101,
                    likesCount = 34,
                    isLikedByMe = true
                )
            )

            val post2Id = communityDao.insertPost(
                CommunityPostEntity(
                    id = 2,
                    authorName = "DJ Alex",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
                    textContent = "I just uploaded my new track 'Urban Gravity' to the platform server! The high-quality audio processing and automatic sync lyrics generated seamlessly! Let me know what you guys think!",
                    attachedTrackId = 104,
                    likesCount = 58,
                    isLikedByMe = false
                )
            )

            communityDao.insertComment(
                CommentEntity(
                    postId = post1Id,
                    authorName = "Marcus Vibe",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop",
                    commentText = "Agreed! That synth breakdown at 01:06 hits so hard with custom 5-band EQ settings."
                )
            )

            communityDao.insertComment(
                CommentEntity(
                    postId = post2Id,
                    authorName = "Elena Bass",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
                    commentText = "Downloaded it for offline listening! Fantastic sub-bass mix! 🔥"
                )
            )
        }
    }
}
