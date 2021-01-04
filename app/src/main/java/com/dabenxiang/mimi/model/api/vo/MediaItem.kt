package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MediaItem (
    @SerializedName("images")
    var picParameter: ArrayList<PicParameter> = arrayListOf(),

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
    var id: String = "",

    @SerializedName("url")
    var url: String = "",

    @SerializedName("length")
    var length: String = "",

    @SerializedName("ext")
    var ext: String = "",
)