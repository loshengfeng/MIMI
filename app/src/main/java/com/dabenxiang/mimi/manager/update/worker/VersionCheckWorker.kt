package com.dabenxiang.mimi.manager.update.worker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import tw.gov.president.manager.BaseManagerData.App
import tw.gov.president.manager.BaseManagerData.configData
import com.dabenxiang.mimi.manager.update.APKDownloaderManager
import com.dabenxiang.mimi.manager.update.VersionManager
import com.dabenxiang.mimi.manager.update.VersionManager.Companion.KEY_FILE_NAME
import com.dabenxiang.mimi.manager.update.VersionManager.Companion.KEY_PATH
import com.dabenxiang.mimi.manager.update.callback.DownloadProgressCallback
import com.dabenxiang.mimi.manager.update.data.FileStatus
import tw.gov.president.manager.worker.BaseWorker

class VersionCheckWorker(var context: Context, params: WorkerParameters) :
    BaseWorker(context, params), KoinComponent {

    private val versionManager: VersionManager by inject()

    @SuppressLint("SdCardPath")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val fileName = inputData.getString(KEY_FILE_NAME)
        val packagesItem = versionManager.fetchPackagesInfo()
        val path = getFilePath(App!!, packagesItem?.versionItem!!.name)
        val data = Data.Builder().putString(KEY_PATH, path).build()
        if (configData?.appVersion ?: 0 < packagesItem.versionItem.major) {
            when (versionManager.getDownloadFileStatus(packagesItem)) {
                FileStatus.NOT_EXIST, FileStatus.NOT_LATEST_FILE -> {
                    versionManager.registerContentObserver(progressCallback)
                    versionManager.downloadFile(packagesItem)
                }
                FileStatus.LATEST_FILE -> {
                    Result.success(data)
                }
            }
        }
        Result.success(data)
    }

    private val progressCallback = object :
        DownloadProgressCallback {
        override fun schedule(longId: Long, totalSize: Int, currentSize: Int, status: Int) {
            Timber.d("RegisterProgressListener schedule download: ${((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()}%")
        }

        override fun complete(longId: Long, path: String, mimeType: String) {
            versionManager.unregisterContentObserver()
            val data = workDataOf("path" to path)
            Result.success(data)
            Timber.d("RegisterProgressListener complete!")
        }
    }

    private fun getFilePath(context: Application, fileName: String?, type:String = APKDownloaderManager.TYPE_APK): String {
        return context.getExternalFilesDir(type)?.absolutePath?.plus("/")
            .plus(fileName)
    }
}
