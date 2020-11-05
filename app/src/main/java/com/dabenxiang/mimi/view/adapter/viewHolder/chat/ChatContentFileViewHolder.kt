package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.widget.utility.FileUtil
import java.io.File

class ChatContentFileViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val txtMessage = itemView.findViewById(R.id.txt_message) as TextView
    private val imgFileIcon = itemView.findViewById(R.id.img_file_icon) as ImageView
    private val txtDownloadState = itemView.findViewById(R.id.txt_download_state) as TextView
    private val loadingUpload = itemView.findViewById(R.id.loading_upload) as ProgressBar

    init {
        imgFileIcon.setOnClickListener {
            if (data?.downloadStatus == VideoDownloadStatusType.UPLOADING)
                return@setOnClickListener
            listener.onVideoClick(data, layoutPosition)
        }
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        val context: Context = itemView.context
        val message = StringBuilder()

        message.append(data?.payload?.ext).append("\n").append(data?.payload?.content)
        txtMessage.text = message

        if (File("${FileUtil.getVideoFolderPath(itemView.context)}${data?.payload?.content}${data?.payload?.ext}").exists())
            data?.downloadStatus = VideoDownloadStatusType.FINISH
        else {
            if (data?.downloadStatus == VideoDownloadStatusType.UPLOADING)
                data?.downloadStatus = VideoDownloadStatusType.UPLOADING
            else
                data?.downloadStatus = VideoDownloadStatusType.NORMAL
        }

        when (data?.downloadStatus) {
            VideoDownloadStatusType.NORMAL -> {
                loadingUpload.visibility = View.GONE
                txtDownloadState.visibility = View.GONE
                txtDownloadState.text = ""
            }
            VideoDownloadStatusType.DOWNLOADING -> {
                loadingUpload.visibility = View.GONE
                txtDownloadState.visibility = View.VISIBLE
                txtDownloadState.text = context.getString(R.string.chat_content_file_downloading)
            }
            VideoDownloadStatusType.FINISH -> {
                loadingUpload.visibility = View.GONE
                txtDownloadState.visibility = View.VISIBLE
                txtDownloadState.text =
                    context.getString(R.string.chat_content_file_download_finish)
            }
            VideoDownloadStatusType.UPLOADING -> {
                loadingUpload.visibility = View.VISIBLE
                txtDownloadState.visibility = View.VISIBLE
                txtDownloadState.text = context.getString(R.string.chat_content_file_uploading)
            }
        }
    }
}