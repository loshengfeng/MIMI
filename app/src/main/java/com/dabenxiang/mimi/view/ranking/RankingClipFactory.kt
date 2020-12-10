package com.dabenxiang.mimi.view.ranking

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.VideoItem

class RankingClipFactory constructor(
    private val rankingClipDataSource: RankingClipDataSource
) : DataSource.Factory<Long, VideoItem>() {
    override fun create(): DataSource<Long, VideoItem> {
        return rankingClipDataSource
    }
}