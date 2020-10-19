package com.dabenxiang.mimi.view.inviteviprecord

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ChatListItem

class InviteVipRecordListFactory constructor(
        private val listDataSource: InviteVipRecordListDataSource
) : DataSource.Factory<Long, ChatListItem>() {
    override fun create(): DataSource<Long, ChatListItem> {
        return listDataSource
    }
}