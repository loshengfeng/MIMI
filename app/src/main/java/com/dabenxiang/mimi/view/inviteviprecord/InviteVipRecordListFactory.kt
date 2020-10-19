package com.dabenxiang.mimi.view.inviteviprecord

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem

class InviteVipRecordListFactory constructor(
        private val listDataSource: InviteVipRecordListDataSource
) : DataSource.Factory<Long, ReferrerHistoryItem>() {
    override fun create(): DataSource<Long, ReferrerHistoryItem> {
        return listDataSource
    }
}