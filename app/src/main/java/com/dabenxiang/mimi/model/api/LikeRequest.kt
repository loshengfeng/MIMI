package com.dabenxiang.mimi.model.api

import com.google.gson.annotations.SerializedName

data class LikeRequest(
    // 0: Like,, 1:Dislike
    @SerializedName("type")
    val type: Int?
)