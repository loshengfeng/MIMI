package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MQTTChatItem(
        // 副檔名, 若為純文字就為空白
        @SerializedName("ext")
        val ext: String?,

        @SerializedName("content")
        val content: String?,

        @SerializedName("sendTime")
        val sendTime: String,

        // 0: 文字; 1: 檔案
        @SerializedName("type")
        val type: Int?
)