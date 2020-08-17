package com.dabenxiang.mimi.widget.utility

import android.graphics.Bitmap
import android.util.LruCache

object LruCacheUtils {

    const val ZERO_ID = "0"

    private var lruCache: LruCache<String, Bitmap>
    private var lruArrayCache: LruCache<String, ByteArray>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        lruCache = LruCache(cacheSize)
        lruArrayCache = LruCache(cacheSize)
    }

    fun putLruCache(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun putLruArrayCache(key: String, array: ByteArray) {
        lruArrayCache.put(key, array)
    }

    fun getLruCache(key: String): Bitmap? {
        return lruCache.get(key)
    }

    fun getLruArrayCache(key: String): ByteArray? {
        return lruArrayCache.get(key)
    }
}