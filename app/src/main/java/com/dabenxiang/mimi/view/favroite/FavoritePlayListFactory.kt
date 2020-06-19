package com.dabenxiang.mimi.view.favroite

import androidx.paging.DataSource

class FavoritePlayListFactory constructor(
    private val favoritePlayListDataSource: FavoritePlayListDataSource
) : DataSource.Factory<Long, Any> () {
    override fun create(): DataSource<Long, Any> {
        return favoritePlayListDataSource
    }
}