package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class StatisticsRequest(
    @SerializedName("type") // First=0 , Registered=1, Order=2
    val type: Int = 0,

    @SerializedName("code")
    val code: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("requestId")
    val requestId: String = "",

    @SerializedName("platformId")
    val platformId: String = "",

    @SerializedName("agentId")
    val agentId: String = "",

    @SerializedName("deviceId")
    val deviceId: String = "",

    @SerializedName("amount")
    val amount: String = ""
)