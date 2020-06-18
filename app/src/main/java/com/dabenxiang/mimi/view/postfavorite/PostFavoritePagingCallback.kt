package com.dabenxiang.mimi.view.postfavorite

import com.dabenxiang.mimi.view.home.PagingCallback

interface PostFavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
}