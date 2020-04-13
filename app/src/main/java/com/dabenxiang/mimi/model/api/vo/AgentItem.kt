package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class AgentItem(
    @SerializedName("merchantOwnerId")
    val merchantOwnerId: Int?,

    @SerializedName("merchantName")
    val merchantName: String?
)