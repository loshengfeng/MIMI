package com.dabenxiang.mimi.view.club.topic

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class TopicDetailPostFactory constructor(
    private val topicDetailPostDataSource: TopicDetailPostDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return topicDetailPostDataSource
    }
}
