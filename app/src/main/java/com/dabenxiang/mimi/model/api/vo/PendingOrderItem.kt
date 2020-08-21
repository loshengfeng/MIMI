package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PendingOrderItem(

    @SerializedName("pendingOrders")
    val pendingOrders: Int = 0,

    @SerializedName("pendingOrderLimit")
    val pendingOrderLimit: Int = 0
)