package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ContentItem (
    @SerializedName("images")
    val images: List<ImageItem>?,

    @SerializedName("shortVideos")
    val shortVideoItem: ShortVideoItem?
)