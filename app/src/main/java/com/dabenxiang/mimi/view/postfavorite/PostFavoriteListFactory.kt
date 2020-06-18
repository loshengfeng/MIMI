package com.dabenxiang.mimi.view.postfavorite

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.PlayListItem

class PostFavoriteListFactory constructor(
    private val postFavoriteListDataSource: PostFavoriteListDataSource,
    private val playlistType : Int = 1,
    private val isAdult: Boolean = false
) : DataSource.Factory<Long, PlayListItem> () {
    override fun create(): DataSource<Long, PlayListItem> {
        return postFavoriteListDataSource
    }
}