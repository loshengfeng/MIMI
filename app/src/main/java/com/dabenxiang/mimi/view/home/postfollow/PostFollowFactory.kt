package com.dabenxiang.mimi.view.home.postfollow

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostFollowItem

class PostFollowFactory constructor(
    private val postFollowDataSource: PostFollowDataSource
) : DataSource.Factory<Int, PostFollowItem>() {
    override fun create(): DataSource<Int, PostFollowItem> {
        return postFollowDataSource
    }
}
