package com.dabenxiang.mimi.model.api.vo
import com.google.gson.annotations.SerializedName

data class MeChatItem(
        @SerializedName("lastMessageTime")
        val lastMessageTime: String,

        @SerializedName("message")
        val message: String,

        @SerializedName("name")
        val name: String
)