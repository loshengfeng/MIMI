package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeOrderItem(
    @SerializedName("merchantUserFriendlyName")
    val merchantUserFriendlyName: String,

    @SerializedName("packageName")
    val packageName: String,

    @SerializedName("paymentStatus")
    val paymentStatus: Int,

    @SerializedName("paymentType")
    val paymentType: Int,

    @SerializedName("sellingPrice")
    val sellingPrice: Int,

    @SerializedName("status")
    val status: Int
)
