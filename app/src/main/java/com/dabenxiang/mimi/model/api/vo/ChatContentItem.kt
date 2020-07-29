package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ChatContentItem(
        @SerializedName("username")
        val username: String?,

        @SerializedName("payload")
        val payload: ChatContentPayloadItem?,

        val dateTitle: String?
)
