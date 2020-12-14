package com.dabenxiang.mimi.model.enums

import androidx.room.TypeConverter
import com.google.gson.annotations.SerializedName

enum class PostStatus(val value: Int) {
    @SerializedName("0")
    OFFLINE(0),

    @SerializedName("1")
    ONLINE(1)
}

class PostStatusConverter {
    @TypeConverter
    fun fromPostStatus(type: PostStatus): Int {
        return type.ordinal
    }

    @TypeConverter
    fun toPostStatus(value: Int): PostStatus {
        return when (value) {
            0 -> PostStatus.OFFLINE
            else -> PostStatus.ONLINE
        }
    }
}