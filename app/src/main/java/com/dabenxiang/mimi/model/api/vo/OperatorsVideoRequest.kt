package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class OperatorsVideoRequest(
    @SerializedName("id")
    val it: Int?,

    @SerializedName("status")
    val status: Int?
)