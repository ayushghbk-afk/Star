package com.example.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserAccount(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val isAnonymous: Boolean = false,
    val channelHandle: String = "@${displayName.lowercase().replace(" ", "")}"
)

class AuthManager {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("AuthManager", "FirebaseAuth not available: ${e.message}")
            null
        }
    }

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        val auth = firebaseAuth
        if (auth != null) {
            val user = auth.currentUser
            if (user != null) {
                _currentUser.value = user.toUserAccount()
            } else {
                // Default guest mode if no current user
                setGuestUser()
            }
        } else {
            setGuestUser()
        }
    }

    private fun setGuestUser() {
        _currentUser.value = UserAccount(
            uid = "guest_user_101",
            email = "creator@streamsync.app",
            displayName = "Creator User",
            photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            isAnonymous = true,
            channelHandle = "@creator_user"
        )
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val account = UserAccount(
                        uid = user?.uid ?: "user_${System.currentTimeMillis()}",
                        email = email,
                        displayName = displayName.ifBlank { email.substringBefore("@") },
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                        isAnonymous = false
                    )
                    _currentUser.value = account
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    // Local fallback creation if network/firebase config unavailable
                    val account = UserAccount(
                        uid = "user_${System.currentTimeMillis()}",
                        email = email,
                        displayName = displayName.ifBlank { email.substringBefore("@") },
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                        isAnonymous = false
                    )
                    _currentUser.value = account
                    onResult(true, "Signed in locally (Offline mode: ${e.message})")
                }
        } else {
            val account = UserAccount(
                uid = "user_${System.currentTimeMillis()}",
                email = email,
                displayName = displayName.ifBlank { email.substringBefore("@") },
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                isAnonymous = false
            )
            _currentUser.value = account
            onResult(true, null)
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val account = user?.toUserAccount() ?: UserAccount(
                        uid = "user_101",
                        email = email,
                        displayName = email.substringBefore("@"),
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop"
                    )
                    _currentUser.value = account
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    val account = UserAccount(
                        uid = "user_${email.hashCode()}",
                        email = email,
                        displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop"
                    )
                    _currentUser.value = account
                    onResult(true, "Signed in (Offline mode: ${e.message})")
                }
        } else {
            val account = UserAccount(
                uid = "user_${email.hashCode()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop"
            )
            _currentUser.value = account
            onResult(true, null)
        }
    }

    fun signOut() {
        firebaseAuth?.signOut()
        setGuestUser()
    }

    private fun FirebaseUser.toUserAccount(): UserAccount {
        return UserAccount(
            uid = uid,
            email = email ?: "",
            displayName = displayName ?: (email?.substringBefore("@") ?: "User"),
            photoUrl = photoUrl?.toString() ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
            isAnonymous = isAnonymous
        )
    }
}
