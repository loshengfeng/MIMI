package com.dabenxiang.mimi.model.enums

import com.google.gson.annotations.SerializedName

enum class LikeType(val value: Int) {
    @SerializedName("0")
    LIKE(0),

    @SerializedName("1")
    DISLIKE(1)
}
