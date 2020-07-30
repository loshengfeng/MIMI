package com.dabenxiang.mimi.manager.update

import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import tw.gov.president.manager.BaseManagerData
import tw.gov.president.manager.BaseManagerData.App
import tw.gov.president.manager.pref.ManagersPref
import com.dabenxiang.mimi.manager.update.callback.DownloadProgressCallback
import com.dabenxiang.mimi.manager.update.data.DownloadItem

class APKDownloaderManager() : KoinComponent {

    private val pref: ManagersPref by inject()

    companion object {
        const val TYPE_APK = "apk"
        const val CONTENT_URI_DOWNLOAD = "content://downloads/my_downloads"
    }

    private val downloadManager =
        BaseManagerData.App!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val contentUri = Uri.parse(CONTENT_URI_DOWNLOAD)
    private val downloadIds = arrayListOf<Long>()
    private var progressCallback: DownloadProgressCallback? = null

    fun setProgressCallback(callback: DownloadProgressCallback) {
        this.progressCallback = callback
    }

    fun addDownloadUrl(url: String, fileName: String?): Long {
        val uri = Uri.parse(url)
        Timber.d("Download URI: $uri")
        val request = DownloadManager.Request(uri).also {
            // this file path is /sdcard/Android/data/{pkgName}/file/{dirType}/{apk}
            it.setDestinationInExternalFilesDir(
                App,
                TYPE_APK, fileName
            )
            it.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            it.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            it.setTitle(fileName)
        }

        val id = downloadManager.enqueue(request)
        Timber.d("Download id: $id")
        downloadIds.add(id)
        pref.downloadId = id
        return id
    }

    fun registerContentObserver() {
        App!!.contentResolver.registerContentObserver(contentUri, true, contentObserver)
    }

    fun unregisterContentObserver() {
        App!!.contentResolver.unregisterContentObserver(contentObserver)
    }

    fun getDownloadId(): Long {
        val id = pref.downloadId
        return when {
            getDownloadStatus(id) == DownloadManager.STATUS_FAILED || getDownloadStatus(id) == DownloadManager.STATUS_SUCCESSFUL -> {
                pref.downloadId = 0
                pref.downloadId
            }
            else -> id
        }
    }

    fun updateProcessRate(id: Long) {
        Timber.d("UpdateProcessRate id: $id")
        val downloadItem = getDownloadItem(id)
        val currentSize = downloadItem.currentSize
        val totalSize = downloadItem.totalSize
        val status = downloadItem.status
        val fileName = downloadItem.title

        Timber.d("totalSize: $totalSize")
        if (totalSize == -1) return
        if (totalSize > currentSize) {
            progressCallback?.schedule(id, totalSize, currentSize, status)
        } else {
            val mimeType = downloadManager.getMimeTypeForDownloadedFile(id)
            val path = App!!.getExternalFilesDir(TYPE_APK)?.absolutePath.plus("/").plus(fileName)
            progressCallback?.complete(id, path, mimeType)
            downloadIds.remove(id)
            pref.downloadId = 0
        }
    }

    private fun getDownloadStatus(longId: Long): Int {
        return getDownloadItem(longId).status
    }

    private fun getDownloadItem(id: Long): DownloadItem {
        val downloadItem =
            DownloadItem()
        val query = DownloadManager.Query().setFilterById(id)
        downloadManager.query(query).also { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                downloadItem.also {
                    it.currentSize =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) // 已经下载檔案的大小
                    it.totalSize =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) // 下载檔案的總大小
                    it.status =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) // 下载檔案的狀態
                    it.title =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)) // 下載檔案的名稱
                }
                cursor.close()
            }
        }
        return downloadItem
    }

    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            downloadIds.forEach {
                Timber.d("ContentObserver onChange id :$it")
                updateProcessRate(it)
            }
        }
    }
}