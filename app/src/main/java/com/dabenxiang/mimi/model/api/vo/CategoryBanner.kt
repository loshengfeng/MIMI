package com.dabenxiang.mimi.model.api.vo

data class CategoryBanner(
    val bannerCategory: Int,
    val content: String,
    val id: Long,
    val intervals: Int,
    val position: Int,
    val sorting: Int,
    val startTime: String,
    val target: Int,
    val title: String,
    val url: String
)