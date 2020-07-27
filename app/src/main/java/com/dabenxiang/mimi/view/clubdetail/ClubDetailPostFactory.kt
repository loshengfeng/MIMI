package com.dabenxiang.mimi.view.clubdetail

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class ClubDetailPostFactory constructor(
    private val clubDetailPostDataSource: ClubDetailPostDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return clubDetailPostDataSource
    }
}
