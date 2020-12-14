package com.dabenxiang.mimi.model.api.vo

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AdItem(
    @SerializedName("href")
    val href: String = "",

    @SerializedName("targetType")
    val targetType: Int = 0,

    @SerializedName("target")
    val target: String = ""
)

class AdItemConverters {

    @TypeConverter
    fun adItemToJson(value: AdItem) = Gson().toJson(value)

    @TypeConverter
    fun jsonToAdItem(value: String) = Gson().fromJson(value, AdItem::class.java)
}