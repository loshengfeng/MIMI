package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeChatItem(
    @SerializedName("name")
    val name: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("lastMessageTime")
    val lastMessageTime: String?
)