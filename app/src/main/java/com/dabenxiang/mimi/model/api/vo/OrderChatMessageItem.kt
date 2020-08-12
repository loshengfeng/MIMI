package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.api.vo.error.OrderChatPayloadItem
import com.google.gson.annotations.SerializedName

data class OrderChatMessageItem(

    @SerializedName("username")
    val username: String = "",

    @SerializedName("payload")
    val payload: OrderChatPayloadItem? = null
)