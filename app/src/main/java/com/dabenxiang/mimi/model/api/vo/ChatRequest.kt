package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("userId")
    val userId: Long?,

    @SerializedName("name")
    val name: String? = ""
)