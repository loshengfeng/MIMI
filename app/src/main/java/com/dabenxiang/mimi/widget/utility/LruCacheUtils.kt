package com.dabenxiang.mimi.widget.utility

import android.graphics.Bitmap
import android.util.LruCache
import com.dabenxiang.mimi.model.api.vo.PlayItem

object LruCacheUtils {

    const val ZERO_ID = "0"

    private var lruCache: LruCache<String, Bitmap>
    private var lruArrayCache: LruCache<String, ByteArray>
    private var shortVideoDataCache: LruCache<Long, PlayItem>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        lruCache = LruCache(cacheSize)
        lruArrayCache = LruCache(cacheSize)
        shortVideoDataCache = LruCache(cacheSize)
    }

    fun putLruCache(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun putLruArrayCache(key: String, array: ByteArray) {
        lruArrayCache.put(key, array)
    }

    fun putShortVideoDataCache(key: Long, playItem: PlayItem) {
        shortVideoDataCache.put(key, playItem)
    }

    fun getLruCache(key: String): Bitmap? {
        return lruCache.get(key)
    }

    fun getLruArrayCache(key: String): ByteArray? {
        return lruArrayCache.get(key)
    }

    fun getShortVideoCount(key: Long): PlayItem? {
        return shortVideoDataCache.get(key)
    }
}

