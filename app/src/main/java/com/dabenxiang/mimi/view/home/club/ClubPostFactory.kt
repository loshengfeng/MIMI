package com.dabenxiang.mimi.view.home.club

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberClubItem

class ClubPostFactory constructor(
    private val clubPostDataSource: ClubPostDataSource
) : DataSource.Factory<Int, MemberClubItem>() {
    override fun create(): DataSource<Int, MemberClubItem> {
        return clubPostDataSource
    }
}