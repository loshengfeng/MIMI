package com.dabenxiang.mimi.manager

import android.graphics.Bitmap
import android.util.LruCache

class LruCacheManager {

    private var lruCache: LruCache<Long, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        lruCache = LruCache<Long, Bitmap>(cacheSize)
    }

    fun putLruCache(key: Long, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun getLruCache(key: Long): Bitmap? {
        return lruCache.get(key)
    }

}