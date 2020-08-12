package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ShortVideoItem(

    @SerializedName("id")
    val id: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("length")
    val length: String = ""
)