package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class BalanceItem (
    @SerializedName("allCount")
    val allCount: Long?,

    @SerializedName("isOnlineCount")
    val isOnlineCount: Long?
)