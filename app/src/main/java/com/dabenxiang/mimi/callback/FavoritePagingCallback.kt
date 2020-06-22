package com.dabenxiang.mimi.callback

interface FavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
}