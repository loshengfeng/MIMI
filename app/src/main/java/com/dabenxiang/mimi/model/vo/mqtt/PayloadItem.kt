package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName

data class PayloadItem(
    @SerializedName("type")
    val type: MessageType = MessageType.TEXT,

    @SerializedName("content")
    val content: String = "",

    @SerializedName("sendTime")
    val sendTime: String = "",

    @SerializedName("ext")
    val ext: String = ""
) {
    enum class MessageType(val value: Int) {
        TEXT(0),
        IMAGE(1),
        BINARY(2)
    }
}