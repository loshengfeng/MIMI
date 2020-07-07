package com.dabenxiang.mimi.model.api.vo.error

import com.google.gson.annotations.SerializedName

data class PostContentItem(

    @SerializedName("images")
    val images: ArrayList<PostImageItem> = arrayListOf(),

    @SerializedName("shortVideos")
    val shortVideo: PostVideoItem
)

data class PostImageItem(

    @SerializedName("id")
    val id: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("ext")
    val ext: String = ""
)

data class PostVideoItem(

    @SerializedName("id")
    val id: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("length")
    val length: String = ""
)