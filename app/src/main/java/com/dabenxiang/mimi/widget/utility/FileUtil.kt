package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.os.StatFs
import com.dabenxiang.mimi.App
import timber.log.Timber
import tw.gov.president.manager.submanager.update.APKDownloaderManager
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    fun getAvailableSpace(context: Context): Long {
        val fs = StatFs(getAppPath(context))
        return fs.availableBytes / 1024 / 1024
    }

    fun getExtFolderPath(context: Context): String {
        val path = StringBuilder(getAppPath(context))
            .append(File.separator)
            .append(CryptUtils.decodeBase64("LkVhZ2xlSG9yc2U="))
            .append(File.separator)
            .toString()
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    fun getVideoFolderPath(context: Context): String {
        val path = StringBuilder(getMediaMountedAppPath(context))
            .append(File.separator)
            .append(CryptUtils.decodeBase64("TUlNSQ=="))
            .append(File.separator)
            .append(CryptUtils.decodeBase64("VmlkZW8="))
            .append(File.separator)
            .toString()
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
    }

    private fun getAppPath(context: Context): String {
//        return when (Environment.MEDIA_MOUNTED) {
//            Environment.getExternalStorageState() -> {
//                Environment.getExternalStorageDirectory().toString()
//            }
//            else -> context.filesDir.toString()
//        }
        return context.filesDir.toString()
    }

    private fun getMediaMountedAppPath(context: Context): String {
        return when (Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageState() -> {
                Environment.getExternalStorageDirectory().toString()
            }
            else -> context.filesDir.toString()
        }
    }

    fun saveBitmapToJpegFile(
        bitmap: Bitmap,
        destWidth: Int = 480,
        destHeight: Int = 640,
        destPath: String
    ) {
        var scale: Bitmap? = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, true)
        try {
            val bos = BufferedOutputStream(FileOutputStream(destPath))
            if (scale!!.compress(Bitmap.CompressFormat.JPEG, 100, bos)) {
                bos.flush()
            }
            bos.close()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            scale?.recycle()
        }
    }

    fun getClipFile(fileName: String): File {
        val dir = File("${getAppPath(App.applicationContext())}/clip")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "/$fileName")
    }

    fun getAvatarFile(): File {
        val dir = File("${getAppPath(App.applicationContext())}/avatar")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir,"/avatar.jpg")
    }

    fun getTakePhoto(fileName: String): File {
        val dir = File("${getAppPath(App.applicationContext())}/pic")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "/$fileName")
    }

    @Suppress(
        "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
        "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
    )
    fun deleteExternalFile(context: Context) {
        File(context.getExternalFilesDir(APKDownloaderManager.TYPE_APK)?.absolutePath).let {
            while (it.listFiles().iterator().hasNext()) {
                it.listFiles().iterator().next().delete()
            }
        }
    }
}
