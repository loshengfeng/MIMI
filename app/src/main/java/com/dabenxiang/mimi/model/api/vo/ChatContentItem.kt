package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.google.gson.annotations.SerializedName

data class ChatContentItem(
        @SerializedName("username")
        val username: String?,

        @SerializedName("payload")
        val payload: ChatContentPayloadItem?,

        val dateTitle: String?,
        var downloadStatus: VideoDownloadStatusType = VideoDownloadStatusType.NORMAL,
        var position: Int = -1
)
