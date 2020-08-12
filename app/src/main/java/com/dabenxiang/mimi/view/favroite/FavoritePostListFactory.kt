package com.dabenxiang.mimi.view.favroite

import androidx.paging.DataSource

class FavoritePostListFactory constructor(
    private val favoritePostListDataSource: FavoritePostListDataSource
) : DataSource.Factory<Long, Any>() {
    override fun create(): DataSource<Long, Any> {
        return favoritePostListDataSource
    }
}