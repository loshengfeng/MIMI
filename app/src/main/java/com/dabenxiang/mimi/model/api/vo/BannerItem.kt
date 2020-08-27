package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class BannerItem(
        @SerializedName("id")
        val id: Long?,

        @SerializedName("title")
        val title: String?,

        @SerializedName("position")
        val position: Int? = 0,

        @SerializedName("url")
        val url: String?,

        @SerializedName("content")
        val content: String?,

        @SerializedName("sorting")
        val sorting: Int? = 0,

        @SerializedName("target")
        val target: Int? = 0,

        @SerializedName("intervals")
        val intervals: Int? = 0,

        @SerializedName("bannerCategory")
        val bannerCategory: Int? = 0,

        @SerializedName("startTime")
        val startTime: String? = "0001-01-01T00:00:00+00:00"
)