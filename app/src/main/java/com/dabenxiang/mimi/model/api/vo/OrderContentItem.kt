package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class OrderContentItem(
    @SerializedName("orders")
    val orders: ArrayList<OrderItem> = arrayListOf(),

    @SerializedName("balance")
    val balance: BalanceItem? = null
)