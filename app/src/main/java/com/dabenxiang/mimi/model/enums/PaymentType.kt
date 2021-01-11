package com.dabenxiang.mimi.model.enums

import com.google.gson.annotations.SerializedName

enum class PaymentType(val value: Int) {
    @SerializedName("1")
    ALI(1),

    @SerializedName("2")
    WX(2),

    @SerializedName("4")
    BANK(4),

    @SerializedName("16")
    TIK_TOK(16)
}