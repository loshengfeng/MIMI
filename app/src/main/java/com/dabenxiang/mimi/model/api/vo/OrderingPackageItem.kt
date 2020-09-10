package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OrderingPackageItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("paymentType")
    val paymentType: PaymentType = PaymentType.BANK,

    @SerializedName("listPrice")
    val listPrice: Float = 0f,

    @SerializedName("price")
    val price: Float = 0f,

    @SerializedName("point")
    val point: Int = 0

) : Serializable