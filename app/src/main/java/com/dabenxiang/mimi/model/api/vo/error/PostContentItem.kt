package com.dabenxiang.mimi.model.api.vo.error

import com.google.gson.annotations.SerializedName

data class PostContentItem(

    @SerializedName("images")
    val images: ArrayList<PostImageItem> = arrayListOf(),

    @SerializedName("shortVideos")
    val shortVideo: PostVideoItem,

    @SerializedName("length")
    val length: String = ""
)

data class PostImageItem(
    @SerializedName("url")
    val url: String = ""
)

data class PostVideoItem(
    @SerializedName("url")
    val url: String = ""
)