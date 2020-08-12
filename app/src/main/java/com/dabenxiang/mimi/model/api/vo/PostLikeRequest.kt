package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PostLikeRequest(
    @SerializedName("type")
    val type: Int
)