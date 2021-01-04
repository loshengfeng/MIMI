package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PaymentInfoItem(

    @SerializedName("merchantAccountId")
    val merchantAccountId: Long = 0,

    @SerializedName("accountName")
    val accountName: String = "",

    @SerializedName("accountNumber")
    val accountNumber: String = "",

    @SerializedName("bankName")
    val bankName: String = "",

    @SerializedName("bankCode")
    val bankCode: String = "",

    @SerializedName("bankBranchName")
    val bankBranchName: String = "",

    @SerializedName("bankBranchProvince")
    val bankBranchProvince: String = "",

    @SerializedName("bankBranchCity")
    val bankBranchCity: String = "",

    @SerializedName("bankBranchAddress")
    val bankBranchAddress: String = "",

    @SerializedName("paymentType")
    val paymentType: PaymentType = PaymentType.BANK,

    @SerializedName("paymentUrl")
    val paymentUrl: String = ""

) : Serializable