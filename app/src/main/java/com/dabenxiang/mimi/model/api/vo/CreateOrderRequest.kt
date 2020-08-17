package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("paymentType")
    val paymentType: PaymentType? = PaymentType.BANK,

    @SerializedName("packageId")
    val packageId: Long = 0
)