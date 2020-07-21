package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class PostMemberRequest (
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("type")
    val type: Int,

    @SerializedName("isAdult")
    val isAdult: Boolean = true,

    @SerializedName("tags")
    val tags: ArrayList<String>
)