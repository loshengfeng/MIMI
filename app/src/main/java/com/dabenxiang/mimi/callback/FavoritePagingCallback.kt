package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem

interface FavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
    fun onTotalVideoId(ids: ArrayList<Long>)
    fun onReceiveResponse(response: ArrayList<PostFavoriteItem>)
}