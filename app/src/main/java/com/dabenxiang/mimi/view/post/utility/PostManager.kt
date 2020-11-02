package com.dabenxiang.mimi.view.post.utility

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.home.AdultHomeFragment
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.REQUEST_VIDEO_CAPTURE
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.RotateUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.snackbar.Snackbar
import com.video.trimmer.utils.RealPathUtil
import com.vincent.videocompressor.VideoCompress
import timber.log.Timber
import java.io.File
import java.util.*

class PostManager {
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
                val bitmap = RotateUtils().rotateImage(file)
                bitmap?.also {
                    FileUtil.saveBitmapToJpegFile(it, it.width, it.height, destPath = file.absolutePath)
                }
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

    fun showSnackBar(rootView: ViewGroup, fragment: Fragment, listener: CancelDialogListener): Snackbar {
        val snackBar = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE)
        val snackBarLayout: Snackbar.SnackbarLayout = snackBar.view as Snackbar.SnackbarLayout
        val textView = snackBarLayout.findViewById(R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        val snackView: View = fragment.layoutInflater.inflate(R.layout.snackbar_upload, null)
        snackBarLayout.addView(snackView, 0)
        snackBarLayout.setPadding(15, 0, 15, 0)
        snackBarLayout.setBackgroundColor(Color.TRANSPARENT)
        snackBar.show()

        val imgCancel = snackBarLayout.findViewById(R.id.iv_cancel) as ImageView
        val txtCancel = snackBarLayout.findViewById(R.id.txt_cancel) as TextView

        txtCancel.setOnClickListener {
            listener.onCancel()
        }

        imgCancel.setOnClickListener {
            listener.onCancel()
        }

        return snackBar
    }

    fun dismissSnackBar(snackBar: Snackbar, postId: Long, memberPostItem: MemberPostItem, viewModel: MainViewModel?, listener: SnackBarListener) {
        val snackBarLayout: Snackbar.SnackbarLayout = snackBar.view as Snackbar.SnackbarLayout
        val progressBar =
            snackBarLayout.findViewById(R.id.contentLoadingProgressBar) as ContentLoadingProgressBar
        val imgSuccess = snackBarLayout.findViewById(R.id.iv_success) as ImageView

        val txtSuccess = snackBarLayout.findViewById(R.id.txt_postSuccess) as TextView
        val txtUploading = snackBarLayout.findViewById(R.id.txt_uploading) as TextView

        val imgCancel = snackBarLayout.findViewById(R.id.iv_cancel) as ImageView
        val txtCancel = snackBarLayout.findViewById(R.id.txt_cancel) as TextView
        val imgPost = snackBarLayout.findViewById(R.id.iv_viewPost) as ImageView
        val txtPost = snackBarLayout.findViewById(R.id.txt_viewPost) as TextView

        progressBar.visibility = View.GONE
        imgSuccess.visibility = View.VISIBLE

        txtSuccess.visibility = View.VISIBLE
        txtUploading.visibility = View.GONE

        imgCancel.visibility = View.GONE
        txtCancel.visibility = View.GONE

        imgPost.visibility = View.VISIBLE
        txtPost.visibility = View.VISIBLE

        memberPostItem.id = postId

        if (viewModel != null) {
            memberPostItem.creatorId = viewModel.pref.profileItem.userId
            memberPostItem.postFriendlyName = viewModel.pref.profileItem.friendlyName
            memberPostItem.avatarAttachmentId = viewModel.pref.profileItem.avatarAttachmentId
        }

        imgPost.setOnClickListener {
            listener.onClick(memberPostItem)
        }

        txtPost.setOnClickListener {
            listener.onClick(memberPostItem)
        }

        Handler().postDelayed({
            snackBar.dismiss()
        }, 3000)
    }

    fun getCompressPath(videoUri: String, context: Context): String {
        val videoUri = Uri.parse(videoUri)
        val file = File(videoUri.path ?: "")
        val destinationPath = Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator
        val root = File(destinationPath)
        val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
        return RealPathUtil.realPathFromUriApi19(context, outputFileUri)
            ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + videoUri.path?.substring(videoUri.path!!.lastIndexOf("/") + 1)).absolutePath
    }

    fun videoCompress(realPath: String, outPutPath: String, listener: VideoCompressListener) {
        VideoCompress.compressVideoLow(realPath, outPutPath , object : VideoCompress.CompressListener {
            override fun onStart() {
                Timber.d("Start compress")
            }

            override fun onSuccess() {
                Timber.d("Compress success")
                listener.onSuccess()
            }

            override fun onFail() {
                Timber.d("Compress fail")
                listener.onFail()
            }

            override fun onProgress(percent: Float) {
                Timber.d("Compress progress : $percent")
            }
        })
    }

    interface CancelDialogListener {
        fun onCancel()
    }

    interface SnackBarListener {
        fun onClick(memberPostItem: MemberPostItem)
    }

    interface VideoCompressListener {
        fun onSuccess()
        fun onFail()
    }
}