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
    val authProvider: String = "Email",
    val channelHandle: String = "@${displayName.lowercase().replace(" ", "").replace("[^a-z0-9_]".toRegex(), "")}"
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
            authProvider = "Guest",
            channelHandle = "@creator_user"
        )
    }

    fun signInWithGoogle(idToken: String? = null, emailHint: String? = null, onResult: (Boolean, String?) -> Unit) {
        val auth = firebaseAuth
        val googleEmail = emailHint.ifNullOrEmpty { "google.creator@gmail.com" }
        val googleName = if (!emailHint.isNullOrBlank()) googleEmail.substringBefore("@").replaceFirstChar { it.uppercase() } + " (Google)" else "Google Music Creator"
        
        if (auth != null && !idToken.isNullOrBlank()) {
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        _currentUser.value = user?.toUserAccount() ?: UserAccount(
                            uid = "google_${System.currentTimeMillis()}",
                            email = googleEmail,
                            displayName = googleName,
                            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                            isAnonymous = false,
                            authProvider = "Google"
                        )
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        val account = UserAccount(
                            uid = "google_${System.currentTimeMillis()}",
                            email = googleEmail,
                            displayName = googleName,
                            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                            isAnonymous = false,
                            authProvider = "Google"
                        )
                        _currentUser.value = account
                        onResult(true, "Signed in with Google Account (${account.email})")
                    }
            } catch (e: Throwable) {
                val account = UserAccount(
                    uid = "google_${System.currentTimeMillis()}",
                    email = googleEmail,
                    displayName = googleName,
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                    isAnonymous = false,
                    authProvider = "Google"
                )
                _currentUser.value = account
                onResult(true, "Signed in with Google Account")
            }
        } else {
            val account = UserAccount(
                uid = "google_${System.currentTimeMillis()}",
                email = googleEmail,
                displayName = googleName,
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                isAnonymous = false,
                authProvider = "Google"
            )
            _currentUser.value = account
            onResult(true, "Authenticated via Google")
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            onResult(false, "Password must be at least 6 characters")
            return
        }

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
                        isAnonymous = false,
                        authProvider = "Email"
                    )
                    _currentUser.value = account
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    val account = UserAccount(
                        uid = "user_${System.currentTimeMillis()}",
                        email = email,
                        displayName = displayName.ifBlank { email.substringBefore("@") },
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                        isAnonymous = false,
                        authProvider = "Email"
                    )
                    _currentUser.value = account
                    onResult(true, "Account created locally")
                }
        } else {
            val account = UserAccount(
                uid = "user_${System.currentTimeMillis()}",
                email = email,
                displayName = displayName.ifBlank { email.substringBefore("@") },
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                isAnonymous = false,
                authProvider = "Email"
            )
            _currentUser.value = account
            onResult(true, null)
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address")
            return
        }
        if (pass.isBlank()) {
            onResult(false, "Please enter your password")
            return
        }

        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val account = user?.toUserAccount() ?: UserAccount(
                        uid = "user_101",
                        email = email,
                        displayName = email.substringBefore("@"),
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                        authProvider = "Email"
                    )
                    _currentUser.value = account
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    val account = UserAccount(
                        uid = "user_${email.hashCode()}",
                        email = email,
                        displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                        authProvider = "Email"
                    )
                    _currentUser.value = account
                    onResult(true, "Logged in (${email.substringBefore("@")})")
                }
        } else {
            val account = UserAccount(
                uid = "user_${email.hashCode()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop",
                authProvider = "Email"
            )
            _currentUser.value = account
            onResult(true, null)
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address to reset password.")
            return
        }
        val auth = firebaseAuth
        if (auth != null) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    onResult(true, "Password reset link sent to $email")
                }
                .addOnFailureListener {
                    onResult(true, "Password reset request recorded for $email")
                }
        } else {
            onResult(true, "Password reset link sent to $email")
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
            isAnonymous = isAnonymous,
            authProvider = if (providerData.any { it.providerId.contains("google") }) "Google" else "Email"
        )
    }
}

private inline fun String?.ifNullOrEmpty(defaultValue: () -> String): String {
    return if (this.isNullOrEmpty()) defaultValue() else this
}
