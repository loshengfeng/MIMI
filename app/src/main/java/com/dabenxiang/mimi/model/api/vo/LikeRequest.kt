package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.google.gson.annotations.SerializedName

data class LikeRequest(
    @SerializedName("type")
    val likeType: LikeType = LikeType.DISLIKE
)