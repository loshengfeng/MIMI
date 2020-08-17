package com.dabenxiang.mimi.view.ranking

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem

class RankingFactory constructor(
    private val rankingDataSource: RankingDataSource
) : DataSource.Factory<Long, PostStatisticsItem>() {
    override fun create(): DataSource<Long, PostStatisticsItem> {
        return rankingDataSource
    }
}