package com.dabenxiang.mimi.callback

interface FavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
    fun onTotalVideoId(ids: ArrayList<Long>)
}