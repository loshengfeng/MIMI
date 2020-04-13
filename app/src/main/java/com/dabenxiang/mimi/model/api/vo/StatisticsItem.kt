package com.dabenxiang.mimi.model.api.vo
import com.google.gson.annotations.SerializedName


data class StatisticsItem(
    @SerializedName("cover")
    val cover: String?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("type")
    val type: Int?
)