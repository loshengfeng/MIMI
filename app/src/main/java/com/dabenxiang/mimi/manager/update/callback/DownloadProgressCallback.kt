package com.dabenxiang.mimi.manager.update.callback

interface DownloadProgressCallback {
    fun schedule(longId: Long, totalSize: Int, currentSize: Int, status: Int)
    fun complete(longId: Long, path: String, mimeType: String)
}