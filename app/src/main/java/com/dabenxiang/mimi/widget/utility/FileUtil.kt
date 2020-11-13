package com.dabenxiang.mimi.widget.utility

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.BuildConfig
import timber.log.Timber
import tw.gov.president.manager.submanager.update.APKDownloaderManager
import java.io.*
import java.nio.charset.StandardCharsets


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
            if (scale!!.compress(Bitmap.CompressFormat.JPEG, 50, bos)) {
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
        return File(dir, "/avatar.jpg")
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

    fun createSecreteFile(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            writeSecreteFileByMediaStore(context)
        } else {
            writeSecreteFile(context)
        }
    }

    fun isSecreteFileExist(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            readSecreteFileByMediaStore(context)
        } else {
            readSecreteFile(context)
        }
    }

    private fun writeSecreteFile(
        context: Context,
        fileName: String = "topSecrete.jpeg",
        content: String = "This is top secret."
    ): Boolean {
        return try {
            val root = File(Environment.getExternalStorageDirectory(), "${Environment.DIRECTORY_PICTURES}/mimi/")
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, fileName)
            val writer = FileWriter(gpxfile)
            writer.append(content)
            writer.flush()
            writer.close()
            toastForDebug(context, "File created successfully")
            true
        } catch (e: IOException) {
            toastForDebug(context, "Fail to create file: $e")
            false
        }
    }

    private fun readSecreteFile(
        context: Context,
        fileName: String = "topSecrete.jpeg"
    ):  Boolean {
        return try {
            val file = File(Environment.getExternalStorageDirectory(), "${Environment.DIRECTORY_PICTURES}/mimi/$fileName")
            val exist = file.exists()

            if (BuildConfig.DEBUG) {
                if (exist) {
                    toastForDebug(context, "$fileName found")
                } else {
                    toastForDebug(context, "Fail to read file")
                }
            }
            exist
        } catch (e: IOException) {
            e.printStackTrace()
            toastForDebug(context, "Fail to read file")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeSecreteFileByMediaStore(
        context: Context,
        fileName: String = "topSecrete",
        content: String = "This is top secret."
    ): Boolean {
        return try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) //file name
            values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "image/jpeg"
            ) //file extension, will automatically add to file
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/mimi/"
            )
            //end "/" is not mandatory
            val uri: Uri = context.contentResolver.insert(
                MediaStore.Images.Media.getContentUri("external"),
                values
            ) ?: Uri.EMPTY //important!
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            outputStream?.write(content.toByteArray())
            outputStream?.close()
            toastForDebug(context, "File created successfully")
            true
        } catch (e: IOException) {
            toastForDebug(context, "Fail to create file")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun readSecreteFileByMediaStore(context: Context, targetName: String = "topSecrete"): Boolean {
        val contentUri = MediaStore.Images.Media.getContentUri("external")

        val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"

        val selectionArgs = arrayOf(Environment.DIRECTORY_PICTURES + "/mimi/")

        val cursor: Cursor? =
            context.contentResolver.query(contentUri, null, selection, selectionArgs, null)

        var uri: Uri? = null

        return if (cursor?.count == 0) {
            toastForDebug(context, "No file found in \"" + Environment.DIRECTORY_PICTURES + "/mimi/\"")
            false
        } else {
            cursor?.run {
                while (this.moveToNext()) {
                    val fileName: String =
                        this.getString(this.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                    if (fileName.contains(targetName)) {
                        val id: Long =
                            this.getLong(this.getColumnIndex(MediaStore.MediaColumns._ID))
                        uri = ContentUris.withAppendedId(contentUri, id)
                        break
                    }
                }
            }

            if (uri == null) {
                Toast.makeText(context, "\"$targetName\" not found", Toast.LENGTH_SHORT).show()
                false
            } else {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri ?: Uri.EMPTY)
                    val size = inputStream?.available() ?: 0
                    val bytes = ByteArray(size)
                    inputStream?.read(bytes)
                    inputStream?.close()
                    val jsonString = String(bytes, StandardCharsets.UTF_8)
                    toastForDebug(context, "\"$targetName\" found: $jsonString")
                    true
                } catch (e: IOException) {
                    toastForDebug(context, "Fail to read file")
                    false
                }
            }
        }
    }

    private fun toastForDebug(context: Context, msg: String) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
