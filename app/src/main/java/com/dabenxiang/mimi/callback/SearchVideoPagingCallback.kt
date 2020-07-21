package com.dabenxiang.mimi.callback

interface SearchVideoPagingCallback : PagingCallback {
    fun onTotalCount(count: Long)
}