package com.dabenxiang.mimi.view.search.post

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class SearchPostFactory constructor(
    private val searchPostDataSource: SearchPostDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return searchPostDataSource
    }
}