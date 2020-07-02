package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class StatisticsItem(
    @SerializedName("count")
    val count: Long?,
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("years")
    val years: Long?
)