package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.google.gson.annotations.SerializedName

data class ChatContentItem(
        @SerializedName("username")
        val username: String? = "",

        @SerializedName("avatarAttachmentId")
        val avatarAttachmentId: String? = "",

        @SerializedName("payload")
        val payload: ChatContentPayloadItem? = null,

        val dateTitle: String? = "",
        var downloadStatus: VideoDownloadStatusType = VideoDownloadStatusType.NORMAL,
        var position: Int = -1,
        var mediaHashCode: Int = 0
)
