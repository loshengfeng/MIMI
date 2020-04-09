package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoSearchItem(
    @SerializedName("cover")
    val cover: String?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("title")
    val title: String?
)
