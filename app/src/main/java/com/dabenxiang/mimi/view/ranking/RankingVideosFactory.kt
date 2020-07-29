package com.dabenxiang.mimi.view.ranking

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.StatisticsItem

class RankingVideosFactory constructor(
    private val rankingVideosDataSource: RankingVideosDataSource
) : DataSource.Factory<Long, StatisticsItem>() {
    override fun create(): DataSource<Long, StatisticsItem> {
        return rankingVideosDataSource
    }
}