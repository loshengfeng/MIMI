package com.dabenxiang.mimi.view.postfavorite

import com.dabenxiang.mimi.callback.PagingCallback

interface PostFavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
}