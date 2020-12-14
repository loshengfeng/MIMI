package com.dabenxiang.mimi.model.enums

import androidx.room.TypeConverter

enum class LikeType(val value: Int) {
    LIKE(0),
    DISLIKE(1);

    companion object {
        fun getByValue(target: Int?): LikeType? {
            return when(target){
                0->LIKE
                1->DISLIKE
                else-> null
            }
        }
    }
}

class LikeTypeConverter {
    @TypeConverter
    fun fromLikeType(type: LikeType): Int {
        return type.ordinal
    }

    @TypeConverter
    fun toLikeType(value: Int): LikeType? {
        return when (value) {
            0 -> LikeType.LIKE
            1 -> LikeType.DISLIKE
            else -> null
        }
    }
}