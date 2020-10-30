package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName

data class OrderPayloadItem(
    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("orderPaymentInfo")
    val orderPaymentInfoItem: OrderPaymentInfoItem? = null

) : PayloadItem()