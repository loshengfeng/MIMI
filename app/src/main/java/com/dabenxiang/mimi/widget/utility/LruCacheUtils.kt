package com.dabenxiang.mimi.widget.utility

import android.graphics.Bitmap
import android.util.LruCache

object LruCacheUtils {

    private var lruCache: LruCache<Long, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        lruCache = LruCache(cacheSize)
    }

    fun putLruCache(key: Long, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun getLruCache(key: Long): Bitmap? {
        return lruCache.get(key)
    }
}