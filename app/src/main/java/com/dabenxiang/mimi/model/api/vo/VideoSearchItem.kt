package com.dabenxiang.mimi.model.api.vo

data class VideoSearchItem(
    val content: List<Content>,
    val paging: PagingItem,
    val code: Long,
    val message: String
) {
    data class Content(
        val id: Long,
        val title: String,
        val cover: String
    )
}

