package com.dabenxiang.mimi.view.chathistory

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ChatListItem

class ChatHistoryListFactory constructor(
        private val listDataSource: ChatHistoryListDataSource
) : DataSource.Factory<Long, ChatListItem>() {
    override fun create(): DataSource<Long, ChatListItem> {
        return listDataSource
    }
}