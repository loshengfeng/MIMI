package com.dabenxiang.mimi.model.api.vo

data class CategoryBanner(
    val id: Long,
    val title: String,
    val position: Int,
    val url: String,
    val content: String,
    val sorting: Int,
    val target: Int,
    val intervals: Int,
    val bannerCategory: Int,
    val startTime: String
)