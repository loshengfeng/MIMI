package com.dabenxiang.mimi.view.myfollow

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem

class MemberFollowListFactory constructor(
    private val memberFollowListDataSource: MemberFollowListDataSource
) : DataSource.Factory<Long, MemberFollowItem>() {
    override fun create(): DataSource<Long, MemberFollowItem> {
        return memberFollowListDataSource
    }
}