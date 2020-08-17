package com.dabenxiang.mimi.view.player

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.vo.BaseVideoItem

class GuessLikeFactory constructor(private val dataSource: GuessLikeDataSource) :
    DataSource.Factory<Long, BaseVideoItem>() {
    override fun create(): DataSource<Long, BaseVideoItem> {
        return dataSource
    }
}