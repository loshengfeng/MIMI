package com.dabenxiang.mimi.view.search.post.keyword

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagDataSource

class SearchPostByKeywordFactory constructor(
    private val searchPostByKeywordDataSource: SearchPostByKeywordDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return searchPostByKeywordDataSource
    }
}