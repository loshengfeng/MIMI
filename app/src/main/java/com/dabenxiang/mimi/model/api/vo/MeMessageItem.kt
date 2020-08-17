package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeMessageItem(
    @SerializedName("content")
    val content: String?,

    @SerializedName("userId")
    val userId: Int?,

    @SerializedName("type")
    val type: Int?,

    @SerializedName("creationDate")
    val creationDate: String?
)