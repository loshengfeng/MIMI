package com.dabenxiang.mimi.view.home.category

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.vo.BaseVideoItem

class CategoriesFactory constructor(private val dataSource: CategoriesDataSource) :
    DataSource.Factory<Long, BaseVideoItem>() {
    override fun create(): DataSource<Long, BaseVideoItem> {
        return dataSource
    }
}