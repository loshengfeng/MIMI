package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class StatisticsItem(

    @SerializedName("id")
    val id: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("cover")
    val cover: String?,

    @SerializedName("source")
    val source: String?,

    @SerializedName("count")
    val count: Int? = 0,

    @SerializedName("years")
    val years: Int? = 0
)