package com.dabenxiang.mimi.model.vo.mqtt

import com.dabenxiang.mimi.model.enums.NotifyType
import com.google.gson.annotations.SerializedName

data class OrderPayloadItem(

    @SerializedName("type")
    val type: NotifyType = NotifyType.CREATE_ORDER,

    @SerializedName("orderId")
    val orderId: Long = 0,

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

    @SerializedName("createTime")
    val createTime: String = "",

    @SerializedName("isSuccessful")
    val isSuccessful: Boolean = false
)