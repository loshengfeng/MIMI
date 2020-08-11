package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.TraceLogStatus
import com.google.gson.annotations.SerializedName

data class OrderChatContentItem(

    @SerializedName("traceLogStatus")
    val traceLogStatus: TraceLogStatus = TraceLogStatus.NOT_START,

    @SerializedName("messages")
    val messages: ArrayList<OrderChatMessageItem> = arrayListOf()
)