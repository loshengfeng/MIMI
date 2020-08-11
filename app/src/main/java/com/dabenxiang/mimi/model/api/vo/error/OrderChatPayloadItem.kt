package com.dabenxiang.mimi.model.api.vo.error

import com.dabenxiang.mimi.model.enums.PayloadType
import com.google.gson.annotations.SerializedName

data class OrderChatPayloadItem(

    @SerializedName("type")
    val payloadType: PayloadType = PayloadType.TEXT,

    @SerializedName("content")
    val content: String = "",

    @SerializedName("sendTime")
    val sendTime: String = "",

    @SerializedName("ext")
    val ext: String = ""
)