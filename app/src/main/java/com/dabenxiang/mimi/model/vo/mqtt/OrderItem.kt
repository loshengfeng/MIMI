package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName

data class OrderItem(

    @SerializedName("clientId")
    val clientId: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("payload")
    val orderPayloadItem: OrderPayloadItem? = null
)