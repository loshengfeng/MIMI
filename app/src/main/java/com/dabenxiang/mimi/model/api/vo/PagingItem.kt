package com.dabenxiang.mimi.model.api.vo

data class PagingItem(
    val offset: Long,
    val limit: Long,
    val pages: Long,
    val count: Long
)