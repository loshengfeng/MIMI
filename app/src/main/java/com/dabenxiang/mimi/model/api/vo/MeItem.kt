package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeItem(
    @SerializedName("availablePoint")
    val availablePoint: Int,

    @SerializedName("friendlyName")
    val friendlyName: String,

    @SerializedName("hasNewMessage")
    val hasNewMessage: Boolean
)
