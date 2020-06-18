package com.dabenxiang.mimi.view.player

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.player.GuessLikeDataSource

class GuessLikeFactory constructor(private val dataSource: GuessLikeDataSource) : DataSource.Factory<Long, BaseVideoItem>() {
    override fun create(): DataSource<Long, BaseVideoItem> {
        return dataSource
    }
}