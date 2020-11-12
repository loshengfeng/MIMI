package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PackageStatusItem(
    @SerializedName("agentPayDisabled")
    val agentPayDisabled: Boolean,

    @SerializedName("onlinePayDisabled")
    val onlinePayDisabled: Boolean,

    @SerializedName("paymentTypes")
    val paymentTypes: ArrayList<PaymentTypeItem> = arrayListOf(),

    @SerializedName("kbcPaymentTypes")
    val kbcPaymentTypes: ArrayList<PaymentTypeItem> = arrayListOf()
)