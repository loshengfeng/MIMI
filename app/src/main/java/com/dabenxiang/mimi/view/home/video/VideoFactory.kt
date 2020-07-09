package com.dabenxiang.mimi.view.home.video

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.holder.BaseVideoItem

class VideoFactory constructor(private val dataSource: VideoDataSource) :
    DataSource.Factory<Long, BaseVideoItem>() {
    override fun create(): DataSource<Long, BaseVideoItem> {
        return dataSource
    }
}