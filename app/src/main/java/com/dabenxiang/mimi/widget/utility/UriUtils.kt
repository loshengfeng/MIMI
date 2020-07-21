package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object UriUtils {
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver?.query(contentUri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val uriStr = cursor?.getString(columnIndex!!)
        cursor?.close()
        return uriStr
    }
}