package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class StreamSyncApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            android.util.Log.w("StreamSyncApp", "FirebaseApp init caught: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return try {
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50 * 1024 * 1024) // 50 MB
                        .build()
                }
                .crossfade(true)
                .allowHardware(true)
                .respectCacheHeaders(false)
                .build()
        } catch (e: Throwable) {
            ImageLoader.Builder(this).build()
        }
    }
}
