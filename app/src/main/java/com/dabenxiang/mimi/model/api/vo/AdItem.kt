package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class AdItem(
    @SerializedName("href")
    val href: String = "",

    @SerializedName("targetType")
    val targetType: Int = 0,

    @SerializedName("target")
    val target: String = ""
)