package com.dabenxiang.mimi.view.chatcontent

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ChatContentItem

class ChatContentListFactory constructor(
    private val listDataSource: ChatContentListDataSource
) : DataSource.Factory<Long, ChatContentItem>() {
    override fun create(): DataSource<Long, ChatContentItem> {
        return listDataSource
    }
}