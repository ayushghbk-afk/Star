package com.example.data.firestore

import android.util.Log
import com.example.data.db.CommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Firebase Firestore is not initialized: ${e.message}")
            null
        }
    }

    suspend fun syncTrack(track: TrackEntity) {
        val db = firestore ?: return
        try {
            val firestoreTrack = FirestoreTrack.fromTrackEntity(track)
            val docRef = db.collection("tracks").document(track.id.toString())
            docRef.set(firestoreTrack).await()
            Log.d("FirestoreSyncManager", "Track ${track.id} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error syncing track ${track.id} to Firestore: ${e.message}")
        }
    }

    suspend fun syncPlaylist(playlist: PlaylistEntity, trackIds: List<Long> = emptyList()) {
        val db = firestore ?: return
        try {
            val firestorePlaylist = FirestorePlaylist.fromPlaylistEntity(playlist, trackIds)
            val docRef = db.collection("playlists").document(playlist.id.toString())
            docRef.set(firestorePlaylist).await()
            Log.d("FirestoreSyncManager", "Playlist ${playlist.id} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error syncing playlist ${playlist.id} to Firestore: ${e.message}")
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        val db = firestore ?: return
        try {
            db.collection("playlists").document(playlistId.toString()).delete().await()
            Log.d("FirestoreSyncManager", "Playlist $playlistId deleted from Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error deleting playlist $playlistId from Firestore: ${e.message}")
        }
    }

    suspend fun syncCommunityPost(post: CommunityPostEntity) {
        val db = firestore ?: return
        try {
            val firestorePost = FirestoreCommunityPost.fromCommunityPostEntity(post)
            val docRef = db.collection("community_posts").document(post.id.toString())
            docRef.set(firestorePost).await()
            Log.d("FirestoreSyncManager", "Community post ${post.id} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error syncing post ${post.id} to Firestore: ${e.message}")
        }
    }

    suspend fun syncComment(comment: CommentEntity) {
        val db = firestore ?: return
        try {
            val firestoreComment = FirestoreComment.fromCommentEntity(comment)
            val docRef = db.collection("comments").document(comment.id.toString())
            docRef.set(firestoreComment).await()
            Log.d("FirestoreSyncManager", "Comment ${comment.id} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error syncing comment ${comment.id} to Firestore: ${e.message}")
        }
    }

    suspend fun fetchRemoteTracks(): List<TrackEntity> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("tracks").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreTrack::class.java)?.toTrackEntity()
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error fetching remote tracks: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchRemotePosts(): List<CommunityPostEntity> {
        val db = firestore ?: return emptyList()
        return try {
            val snapshot = db.collection("community_posts").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreCommunityPost::class.java)?.toCommunityPostEntity()
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error fetching remote posts: ${e.message}")
            emptyList()
        }
    }
}
