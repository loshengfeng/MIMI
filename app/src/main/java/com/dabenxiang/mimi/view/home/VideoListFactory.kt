package com.dabenxiang.mimi.view.home

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.holder.BaseVideoItem

class VideoListFactory constructor(private val dataSource: VideoListDataSource) :
    DataSource.Factory<Long, BaseVideoItem>() {
    override fun create(): DataSource<Long, BaseVideoItem> {
        return dataSource
    }
}