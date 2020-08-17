package com.dabenxiang.mimi.view.search.post.tag

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagDataSource

class SearchPostByTagFactory constructor(
    private val searchPostByTagDataSource: SearchPostByTagDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return searchPostByTagDataSource
    }
}