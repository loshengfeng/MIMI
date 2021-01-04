package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class BlockAdItem(
    @SerializedName("blockCode")
    val blockCode: String = "",

    @SerializedName("ad")
    val ad: ArrayList<AdItem> = arrayListOf()
)