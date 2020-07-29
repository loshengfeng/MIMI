package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class ChatListItem(
        @SerializedName("id")
        val id: Long?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("message")
        val message: String?,

        @SerializedName("avatarAttachmentId")
        val avatarAttachmentId: Long?,

        @SerializedName("lastMessageTime")
        val lastMessageTime: Date?,

        @SerializedName("lastReadTime")
        val lastReadTime: Date?
) : Serializable
