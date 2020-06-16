package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("chatId")
    val chatId: Int?,

    @SerializedName("userFriendlyName")
    val userFriendlyName: String?,

    // 禮包名稱
    @SerializedName("packageName")
    val packageName: String?,

    @SerializedName("packageListPrice")
    val packageListPrice: Int?,

    @SerializedName("packagePrice")
    val packagePrice: Int?,

    @SerializedName("packagePoint")
    val packagePoint: Int?,

    // 客服名稱
    @SerializedName("merchantUserFriendlyName")
    val merchantUserFriendlyName: String?,

    // 收款類別 0:None|1:Alipay|2:WeChat|4:UnionPay
    @SerializedName("paymentType")
    val paymentType: Int?,

    // 付款狀態 0:Unpaid|1:Paid|99:Failed
    @SerializedName("paymentStatus")
    val paymentStatus: Int?,

    // 付款金額
    @SerializedName("sellingPrice")
    val sellingPrice: Int?,

    @SerializedName("status")
    val status: Int?,

    @SerializedName("createTime")
    val createTime: String?,

    @SerializedName("completionTime")
    val completionTime: String?,

    @SerializedName("accountName")
    val accountName: String?,

    @SerializedName("accountNumber")
    val accountNumber: String?
)