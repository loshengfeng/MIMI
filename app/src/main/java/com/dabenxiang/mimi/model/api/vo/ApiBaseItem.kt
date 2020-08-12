package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ApiBaseItem<T>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("content")
    val content: T?
)
