package com.dabenxiang.mimi.model.vo.mqtt

import com.google.gson.annotations.SerializedName

data class MsgItem(
    @SerializedName("clientId")
    val clientId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("payload")
    val payload: PayloadItem
)