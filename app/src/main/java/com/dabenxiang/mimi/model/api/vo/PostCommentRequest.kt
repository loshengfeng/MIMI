package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PostCommentRequest(
    @SerializedName("parentId")
    val parentId: Long?,

    @SerializedName("content")
    val content: String
)