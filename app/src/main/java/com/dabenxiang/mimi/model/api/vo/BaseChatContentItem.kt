package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

open class BaseChatContentItem(
    @SerializedName("messages")
    val messages: ArrayList<ChatContentItem> = arrayListOf()
)