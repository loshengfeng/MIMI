package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MediaItem (
    @SerializedName("images")
    val picParameter: ArrayList<PicParameter> = arrayListOf(),

    @SerializedName("shortVideos")
    var videoParameter: VideoParameter = VideoParameter(),

    @SerializedName("text")
    var textContent: String = ""
)

data class PicParameter(
    @SerializedName("id")
    var id: String = "",

    @SerializedName("ext")
    val ext: String = "",

    @SerializedName("url")
    val url: String = ""
)

data class VideoParameter(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("length")
    val length: String = ""
)