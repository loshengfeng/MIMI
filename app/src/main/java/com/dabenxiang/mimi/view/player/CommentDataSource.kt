package com.dabenxiang.mimi.view.player

import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.dabenxiang.mimi.view.base.JFPageDataSource

class CommentDataSource(loadMoreModule: BaseLoadMoreModule) : JFPageDataSource<Long, List<String>>(loadMoreModule) {
    override suspend fun load(params: Long?): JFLoadResult {
        return JFLoadResult.Page(null, listOf("AAA", "BB"))
    }
}