package com.dabenxiang.mimi.view.home.club

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberClubItem

class ClubFactory constructor(
    private val clubDataSource: ClubDataSource
) : DataSource.Factory<Int, MemberClubItem>() {
    override fun create(): DataSource<Int, MemberClubItem> {
        return clubDataSource
    }
}