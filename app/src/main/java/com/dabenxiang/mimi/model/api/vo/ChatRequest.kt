package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("userId")
    val userId: String?,

    @SerializedName("name")
    val friendlyName: String?
)