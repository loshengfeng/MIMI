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
    fun adItemToJson(item: AdItem?): String = item?.let{
        Gson().toJson(it)
    } ?: Gson().toJson("")

    @TypeConverter
    fun jsonToAdItem(value: String):AdItem? = value.takeIf { it.isEmpty() }?.let{
        Gson().fromJson(value, AdItem::class.java)
    } ?: run {
        null
    }
}