package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class CountItem(
    @SerializedName("likeCount")
    val likeCount: Long = 0,

    @SerializedName("dislikeCount")
    val dislikeCount: Long = 0,

    @SerializedName("favoriteCount")
    val favoriteCount: Long = 0,

    @SerializedName("followCount")
    val followCount: Long = 0,

    @SerializedName("commentCount")
    val commentCount: Long = 0,
)