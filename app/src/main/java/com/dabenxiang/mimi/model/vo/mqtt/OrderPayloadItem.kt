package com.dabenxiang.mimi.model.vo.mqtt

import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName
import java.util.*

data class OrderPayloadItem(

    @SerializedName("orderId")
    val orderId: String = "",

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

    @SerializedName("BankBranchProvince")
    val bankBranchProvince: String = "",

    @SerializedName("amount")
    val amount: Float = 0f,

    @SerializedName("CreateTime")
    val createTime: Date? = null,

    @SerializedName("isSuccessful")
    val isSuccessful: Boolean = false,

    @SerializedName("paymentType")
    val paymentType: PaymentType = PaymentType.BANK

) : PayloadItem()