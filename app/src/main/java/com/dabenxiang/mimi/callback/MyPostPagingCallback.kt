package com.dabenxiang.mimi.callback

interface MyPostPagingCallback : PagingCallback {
    fun onTotalCount(count: Long, isInitial: Boolean) {}
}