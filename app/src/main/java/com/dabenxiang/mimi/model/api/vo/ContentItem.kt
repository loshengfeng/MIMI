package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ContentItem(

    @SerializedName("images")
    val images: ArrayList<ImageItem>? = arrayListOf(),

    @SerializedName("shortVideos")
    val shortVideo: ShortVideoItem?
)