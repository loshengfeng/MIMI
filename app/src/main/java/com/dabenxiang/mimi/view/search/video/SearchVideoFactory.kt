package com.dabenxiang.mimi.view.search.video

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.VideoItem

class SearchVideoFactory constructor(private val dataSource: SearchVideoListDataSource) :
    DataSource.Factory<Long, VideoItem>() {
    override fun create(): DataSource<Long, VideoItem> {
        return dataSource
    }
}