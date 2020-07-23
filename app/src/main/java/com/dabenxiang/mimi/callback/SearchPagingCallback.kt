package com.dabenxiang.mimi.callback

interface SearchPagingCallback : PagingCallback {
    fun onTotalCount(count: Long)
}