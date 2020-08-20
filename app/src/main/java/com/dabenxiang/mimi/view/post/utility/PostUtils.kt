package com.dabenxiang.mimi.view.post.utility

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.AdultHomeFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.REQUEST_VIDEO_CAPTURE
import com.dabenxiang.mimi.widget.utility.RotateUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import java.io.File

class PostUtils {
    fun selectVideo(fragment: Fragment) {
        val context = fragment.requireContext()

        val galleryIntent = Intent()
        galleryIntent.type = "video/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        val cameraIntent = Intent()
        cameraIntent.action = MediaStore.ACTION_VIDEO_CAPTURE
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, AdultHomeFragment.RECORD_LIMIT_TIME)
        cameraIntent.resolveActivity(context.packageManager)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
        chooser.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.post_select_video))

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        fragment.startActivityForResult(chooser, REQUEST_VIDEO_CAPTURE)
    }

    fun selectPics(fragment: Fragment, file: File) {
        val context = fragment.requireContext()

        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
        chooser.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.post_select_pic))

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        fragment.startActivityForResult(chooser, BasePostFragment.INTENT_SELECT_IMG)
    }

    fun isVideoTimeValid(videoUri: Uri, context: Context): Boolean {
        return getVideoTime(videoUri, context) >= 3000
    }

    fun getVideoTime(videoUri: Uri, context: Context): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
        retriever.release()
        return videoTime
    }

    fun getPicsUri(clipData: ClipData, context: Context): ArrayList<String> {
        val picsUri = arrayListOf<String>()

        for (i in 0 until clipData.itemCount) {
            val item = clipData.getItemAt(i)
            val uri = UriUtils.getPath(context, item.uri)
            picsUri.add(uri.toString())
        }

        return picsUri
    }

    fun getPicUri(data: Intent?, context: Context, file: File): Uri {
        var uri = Uri.parse("")

        if (data?.data == null) {
            val extras = data?.extras

            if (extras == null) {
                RotateUtils().rotateImage(file)
            } else {
                val extrasData = extras["data"]
                val imageBitmap = extrasData as Bitmap?
                uri = Uri.parse(MediaStore.Images.Media.insertImage(context.contentResolver, imageBitmap, null,null))
            }
        } else {
            uri = data.data!!
        }

        return uri
    }
}