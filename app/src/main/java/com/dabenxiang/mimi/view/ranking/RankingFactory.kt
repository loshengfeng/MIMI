package com.dabenxiang.mimi.view.ranking

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.api.vo.RankingItem

class RankingFactory constructor(
    private val rankingDataSource: RankingDataSource
) : DataSource.Factory<Long, RankingItem>() {
    override fun create(): DataSource<Long, RankingItem> {
        return rankingDataSource
    }
}