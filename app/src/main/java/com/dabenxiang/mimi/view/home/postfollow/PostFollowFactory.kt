package com.dabenxiang.mimi.view.home.postfollow

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class PostFollowFactory constructor(
    private val postFollowDataSource: PostFollowDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return postFollowDataSource
    }
}
