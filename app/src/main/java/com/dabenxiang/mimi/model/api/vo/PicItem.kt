package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PicItem (
    @SerializedName("images")
    val picParameter: ArrayList<PicParameter> = arrayListOf()
)

data class PicParameter(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("ext")
    val ext: String = "",

    @SerializedName("url")
    val url: String = ""
)