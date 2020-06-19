package com.dabenxiang.mimi.view.favroite

import com.dabenxiang.mimi.callback.PagingCallback

interface FavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
}