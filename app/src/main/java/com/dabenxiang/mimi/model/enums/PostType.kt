package com.dabenxiang.mimi.model.enums

import com.google.gson.annotations.SerializedName

enum class PostType(val value: Int) {
    @SerializedName("1")
    TEXT(1),

    @SerializedName("2")
    IMAGE(2),

    @SerializedName("4")
    VIDEO(4),

    @SerializedName("8")
    VIDEO_ON_DEMAND(8),

    AD(1024);

    companion object {
        fun getTypeByValue(target: Int?): PostType {
            var result: PostType = TEXT
            values().forEach {
                if (it.value == target) {
                    result = it
                }
            }

            return result
        }
    }
}