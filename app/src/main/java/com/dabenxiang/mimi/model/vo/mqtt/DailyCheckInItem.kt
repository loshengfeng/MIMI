package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName

data class DailyCheckInItem(

    @SerializedName("clientId")
    val clientId: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("payload")
    val dailyCheckInPayLoadItem: DailyCheckInPayLoadItem? = null
)