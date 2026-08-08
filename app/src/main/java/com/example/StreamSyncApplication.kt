package com.example

import android.app.Application
import android.util.Log

class StreamSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.w("StreamSyncApp", "FirebaseApp init caught: ${e.message}")
        }
    }
}
