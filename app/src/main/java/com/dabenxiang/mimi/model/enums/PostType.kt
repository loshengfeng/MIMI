package com.dabenxiang.mimi.model.enums

import androidx.room.TypeConverter
import com.google.gson.annotations.SerializedName

enum class PostType(val value: Int) {
    FOLLOWED(0),

    @SerializedName("1")
    TEXT(1),

    @SerializedName("2")
    IMAGE(2),

    @SerializedName("4")
    VIDEO(4),

    @SerializedName("7")
    TEXT_IMAGE_VIDEO(7),

    @SerializedName("8")
    VIDEO_ON_DEMAND(8),

    @SerializedName("16")
    SMALL_CLIP(16),

    AD(1024);

    companion object {
        fun getTypeByValue(target: Int?): PostType {
            var result: PostType = FOLLOWED
            values().forEach {
                if (it.value == target) {
                    result = it
                }
            }

            return result
        }
    }
}

class PostTypeConverter {
    @TypeConverter
    fun fromPostType(type: PostType): Int {
        return when (type) {
            PostType.FOLLOWED ->0
            PostType.TEXT ->1
            PostType.IMAGE ->2
            PostType.VIDEO -> 4
            PostType.TEXT_IMAGE_VIDEO -> 7
            PostType.VIDEO_ON_DEMAND ->8
           PostType.SMALL_CLIP -> 16
            else -> 1024
        }
    }

    @TypeConverter
    fun toPostType(value: Int): PostType {
        return when (value) {
            0 -> PostType.FOLLOWED
            1 -> PostType.TEXT
            2 -> PostType.IMAGE
            4 -> PostType.VIDEO
            7 -> PostType.TEXT_IMAGE_VIDEO
            8 -> PostType.VIDEO_ON_DEMAND
            16 -> PostType.SMALL_CLIP
            else -> PostType.AD
        }
    }
}