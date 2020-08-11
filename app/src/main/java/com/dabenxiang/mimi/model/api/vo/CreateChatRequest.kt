package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class CreateChatRequest(
    @SerializedName("orderId")
    val orderId: Long = 0
)