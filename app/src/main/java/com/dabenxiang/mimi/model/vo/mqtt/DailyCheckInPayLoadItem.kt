package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName
import java.util.*

data class DailyCheckInPayLoadItem(

    @SerializedName("signInTime")
    val signInTime: Date = Date(),

    @SerializedName("expiryTime")
    val expiryTime: Date = Date(),

    @SerializedName("videoCount")
    val videoCount: Int = 0,

    @SerializedName("videoOnDemandCount")
    val videoOnDemandCount: Int = 0

) : PayloadItem()