package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MsgRequest(
    @SerializedName("chatId")
    val chatId: Int?,

    @SerializedName("type")
    val type: Int?,

    @SerializedName("content")
    val content: String?
)