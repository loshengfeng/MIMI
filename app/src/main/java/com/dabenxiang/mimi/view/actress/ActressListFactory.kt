package com.dabenxiang.mimi.view.actress

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem

class ActressListFactory constructor(
        private val listDataSource: ActressListDataSource
) : DataSource.Factory<Long, ReferrerHistoryItem>() {
    override fun create(): DataSource<Long, ReferrerHistoryItem> {
        return listDataSource
    }
}