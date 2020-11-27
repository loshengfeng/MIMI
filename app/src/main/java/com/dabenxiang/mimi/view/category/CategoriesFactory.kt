package com.dabenxiang.mimi.view.category

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.vo.BaseVideoItem

class CategoriesFactory constructor(private val dataSource: CategoriesDataSource) :
    DataSource.Factory<Int, BaseVideoItem>() {
    override fun create(): DataSource<Int, BaseVideoItem> {
        return dataSource
    }
}