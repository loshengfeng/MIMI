package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class StatisticsRequest(
    @SerializedName("type") // First=0 , Registered=1, Order=2
    val type: Int = 0,

    @SerializedName("code")
    val code: String = ""
)