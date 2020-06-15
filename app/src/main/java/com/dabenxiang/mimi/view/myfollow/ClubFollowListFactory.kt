package com.dabenxiang.mimi.view.myfollow

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem

class ClubFollowListFactory constructor(
    private val clubFollowListDataSource: ClubFollowListDataSource
) : DataSource.Factory<Long, ClubFollowItem>() {
    override fun create(): DataSource<Long, ClubFollowItem> {
        return clubFollowListDataSource
    }
}