package com.dabenxiang.mimi.manager.update

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import tw.gov.president.manager.BaseManagerData.App
import tw.gov.president.manager.BaseManagerData.configData
import tw.gov.president.manager.data.TokenData
import tw.gov.president.manager.pref.ManagersPref
import com.dabenxiang.mimi.manager.update.APKDownloaderManager.Companion.TYPE_APK
import com.dabenxiang.mimi.manager.update.callback.DownloadProgressCallback
import com.dabenxiang.mimi.manager.update.data.FileStatus
import com.dabenxiang.mimi.manager.update.data.PackagesItem
import com.dabenxiang.mimi.manager.update.data.VersionStatus
import com.dabenxiang.mimi.manager.update.worker.VersionCheckWorker
import tw.gov.president.utils.general.app.GeneralUtils.getAndroidID
import java.io.File

class VersionManager : KoinComponent {

    companion object {
        const val KEY_PATH = "path"
        const val KEY_FILE_NAME = "file_name"
        const val MIME_TYPE_APK = "application/vnd.android.package-archive"
    }

    private val pref: ManagersPref by inject()
    private val updateDomainManager: UpdateDomainManager by inject()
    private val downloaderManager: APKDownloaderManager by inject()
    private val pkgManager = App?.packageManager

    fun buildVersionCheckRequest(fileName: String): OneTimeWorkRequest {
        val data = Data.Builder().putString(KEY_FILE_NAME, fileName).build()
        return OneTimeWorkRequest.Builder(VersionCheckWorker::class.java)
            .setInputData(data)
            .build()
    }

    suspend fun checkVersion(): VersionStatus {
        val currentVersion = configData?.appVersion ?: 0
        Timber.i("currentVersion: $currentVersion  ")
        val packagesItem = fetchPackagesInfo() ?: return VersionStatus.UNCHANGED
        Timber.i("packagesItem: $packagesItem  ")
        return try {
            val miniVersion = packagesItem.versionItem!!.major
            val version = packagesItem.versionItem.code.toLong()
            Timber.e("miniVersion: $miniVersion   version: $version")

            when {
                miniVersion > currentVersion -> VersionStatus.FORCE_UPDATE
                version > currentVersion -> VersionStatus.UPDATE
                else -> VersionStatus.UNCHANGED
            }
        } catch (e: Exception) {
            Timber.e("Check Version: $e")
            VersionStatus.UNCHANGED
        }
    }

    suspend fun updateApp(fileName: String, progressCallback: DownloadProgressCallback) {
        val fileInfoItem = fetchPackagesInfo()
        val filePath = getFilePath(App!!, fileInfoItem?.packageUrlItem!!.main)
        when (getDownloadFileStatus(fileInfoItem)) {
            FileStatus.NOT_EXIST, FileStatus.NOT_LATEST_FILE -> {
                Timber.d("updateApp downloadFile.....")
                downloadFile(fileInfoItem)
            }
            FileStatus.LATEST_FILE -> {
                Timber.d("updateApp complete: $filePath")
                progressCallback.complete(
                    -1, filePath,
                    MIME_TYPE_APK
                )
            }
        }
        registerContentObserver(progressCallback)
    }

    suspend fun fetchPackagesInfo(): PackagesItem? {
        Timber.d("fetchPackagesInfo ")
        Timber.d("fetchPackagesInfo updateDomainManager=$updateDomainManager")
        val tokenResult = updateDomainManager.getApiRepository().authToken()
        Timber.d("fetchPackagesInfo tokenResult: ${tokenResult.isSuccessful}")
        if (tokenResult.isSuccessful) {
            tokenResult.body()?.accessToken?.let { auth ->
                pref.updateToken =
                    TokenData(accessToken = auth)
                val result =
                    updateDomainManager.getApiRepository().getPackagesInfo(
                        configData!!.applicationId,
                        getAndroidID()
                    )

                Timber.d("fetchPackagesInfo Info data: ${result.body()?.data}")
                return result.body()?.data?.get(0)
            } ?: return null
        } else {
            return null
        }
    }

    suspend fun bindingInvitationCodes(code: String): Boolean {
        val tokenResult = updateDomainManager.getApiRepository().authToken()
        Timber.d("fetchPackagesInfo tokenResult: ${tokenResult.isSuccessful}")
        if (tokenResult.isSuccessful) {
            tokenResult.body()?.accessToken?.let { auth ->
                pref.updateToken =
                    TokenData(accessToken = auth)
                val bindingResult =
                    updateDomainManager.getApiRepository().bindingInvitationCodes(
                        code, getAndroidID()
                    )

                Timber.d("bindingResult = ${bindingResult}")
                return bindingResult.isSuccessful
            } ?: return false
        } else {
            return false
        }
    }

    fun registerContentObserver(progressCallback: DownloadProgressCallback) {
        downloaderManager.registerContentObserver()
        downloaderManager.setProgressCallback(progressCallback)
    }

    fun unregisterContentObserver() {
        downloaderManager.unregisterContentObserver()
    }

    fun getDownloadFileStatus(packagesItem: PackagesItem?): FileStatus {
        if (checkFileExists(App!!, packagesItem?.versionItem?.name ?: "")) {
            //FIXME
            val filePath = getFilePath(App!!, packagesItem?.versionItem!!.name)
            val packageInfo = pkgManager!!.getPackageArchiveInfo(filePath, 0)
            val apkVersion = getApkVersion(packageInfo)
            return when {
                apkVersion > configData?.appVersion ?: 0 -> FileStatus.LATEST_FILE
                else -> FileStatus.NOT_LATEST_FILE
            }
        }
        return FileStatus.NOT_EXIST
    }

    fun downloadFile(packagesItem: PackagesItem?) {
        val downloadUrl = packagesItem?.packageUrlItem!!.main
        Timber.d("Download Url: $downloadUrl")

        val id = downloaderManager.getDownloadId()
        if (id == 0.toLong()) {
            downloaderManager.addDownloadUrl(downloadUrl, packagesItem.versionItem!!.name)
        } else {
            downloaderManager.updateProcessRate(id)
        }
    }

    fun setupRecordTimestamp() {
        pref.recordTimestamp = System.currentTimeMillis()
    }

    fun getRecordTimestamp(): Long {
        return pref.recordTimestamp
    }

    private fun getApkVersion(packageInfo: PackageInfo?): Long {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> packageInfo!!.longVersionCode
                else -> packageInfo!!.versionCode.toLong()
            }
        } catch (e: Exception) {
            Timber.e("Unzip file or some error")
            (-1).toLong()
        }
    }

    private fun checkFileExists(context: Application, fileName: String?, type:String =TYPE_APK): Boolean {
        return try {
            File(
                getFilePath(
                    context,
                    fileName,
                    type
                )
            ).exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun getFilePath(context: Application, fileName: String?, type:String =TYPE_APK): String {
        return context.getExternalFilesDir(type)?.absolutePath?.plus("/")
            .plus(fileName)
    }
}