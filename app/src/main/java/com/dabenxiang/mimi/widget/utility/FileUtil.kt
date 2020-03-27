package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File

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
        val path = StringBuilder(getAppPath(context))
            .append(File.separator)
            .append(CryptUtils.decodeBase64("RWFnbGVIb3JzZQ=="))
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
        return when (Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageState() -> {
                Environment.getExternalStorageDirectory().toString()
            }
            else -> context.filesDir.toString()
        }
    }

}
