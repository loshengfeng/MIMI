package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class SimpleVideoItem(
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("title")
    val title: String?
)