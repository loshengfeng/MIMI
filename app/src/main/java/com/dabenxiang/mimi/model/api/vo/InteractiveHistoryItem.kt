package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class InteractiveHistoryItem(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("isView")
    val isView: Boolean? = false,

    @SerializedName("isLike")
    val isLike: Boolean? = false,

    @SerializedName("isDislike")
    val isDislike: Boolean? = false,

    @SerializedName("isFavorite")
    val isFavorite: Boolean? = false,

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