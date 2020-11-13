package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName
import java.util.*

data class OrderPayloadItem(

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("isSuccessful")
    val isSuccessful: Boolean = false,

    @SerializedName("accountName")
    val accountName: String = "",

    @SerializedName("accountNumber")
    val accountNumber: String = "",

    @SerializedName("bankCode")
    val bankCode: String = "",

    @SerializedName("bankName")
    val bankName: String = "",

    @SerializedName("bankBranchName")
    val bankBranchName: String = "",

    @SerializedName("bankBranchCity")
    val bankBranchCity: String = "",

    @SerializedName("bankBranchProvince")
    val bankBranchProvince: String = "",

    @SerializedName("amount")
    val amount: Float = 0f,

    @SerializedName("createTime")
    val createTime: Date? = null,

    @SerializedName("paymentType")
    val paymentType: Int = 4

) : PayloadItem()