package com.dabenxiang.mimi.model.enums

import androidx.room.TypeConverter

enum class ClubTabItemType(val value: Int) {
    FOLLOW(0),
    HOTTEST(1),
    LATEST(2),
    SHORT_VIDEO(3),
    PICTURE(4),
    NOVEL(5)
}


class ClubTabItemTypeConverter {
    @TypeConverter
    fun fromClubTabItemType(type: ClubTabItemType): Int {
        return when (type) {
            ClubTabItemType.FOLLOW ->0
            ClubTabItemType.HOTTEST ->1
            ClubTabItemType.LATEST ->2
            ClubTabItemType.SHORT_VIDEO -> 3
            ClubTabItemType.PICTURE -> 4
            else -> 5
        }
    }

    @TypeConverter
    fun toClubTabItemType(value: Int): ClubTabItemType {
        return when (value) {
            0 -> ClubTabItemType.FOLLOW
            1 -> ClubTabItemType.HOTTEST
            2 -> ClubTabItemType.LATEST
            3 -> ClubTabItemType.SHORT_VIDEO
            4 -> ClubTabItemType.PICTURE
            else -> ClubTabItemType.NOVEL
        }
    }
}