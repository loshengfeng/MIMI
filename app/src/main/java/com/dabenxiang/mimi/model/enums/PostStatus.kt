package com.dabenxiang.mimi.model.enums

import com.google.gson.annotations.SerializedName

enum class PostStatus(val value: Int) {
    @SerializedName("0")
    OFFLINE(0),

    @SerializedName("1")
    ONLINE(1)
}