package com.dabenxiang.mimi.model.api.vo.error

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class FansItem(
        @SerializedName("id")
        val id: Long? = 0,

        @SerializedName("name")
        val name: String? = "",

        @SerializedName("message")
        val message: String? = "",

        @SerializedName("avatarAttachmentId")
        val avatarAttachmentId: Long? = 0,

        @SerializedName("lastMessageTime")
        val lastMessageTime: Date? = null,

        @SerializedName("lastReadTime")
        var lastReadTime: Date? = null
) : Serializable
