package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoSearchItem(
    @SerializedName("category")
    val category: Category?,

    @SerializedName("videos")
    val videos: List<VideoItem>?
) {
    data class Category(
//        @SerializedName("categories")
//        val categories: List<Category>?,
//        @SerializedName("name")
//        val name: String?,
        @SerializedName("areas")
        val areas: List<String>?,
        @SerializedName("years")
        val years: List<String>?
    )

    data class VideoSearchDetail(
        @SerializedName("cover")
        val cover: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("favorite")
        val favorite: Boolean?,
        @SerializedName("id")
        val id: Long?,
        @SerializedName("timesWatched")
        val timesWatched: Int?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("updateTime")
        val updateTime: String?,
        @SerializedName("years")
        val years: Int?
    )
}