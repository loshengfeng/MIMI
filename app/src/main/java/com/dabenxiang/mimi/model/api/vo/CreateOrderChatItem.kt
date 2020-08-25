package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class CreateOrderChatItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("chatId")
    val chatId: Long = 0
)