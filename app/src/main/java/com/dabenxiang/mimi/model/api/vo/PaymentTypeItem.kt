package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PaymentTypeItem(

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("disabled")
    val disabled: Boolean? = true,

    @SerializedName("sorting")
    val sorting: Int? = 0
)
