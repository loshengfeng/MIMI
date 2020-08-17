package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class ChatContentPayloadItem(
    @SerializedName("type")
    val type: Int?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("sendTime")
    val sendTime: Date?,

    @SerializedName("ext")
    val ext: String?
)
