package com.dabenxiang.mimi.view.clip

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.VideoItem


/**
 * 固定數量小視頻PagingSource
 */
class ClipLimitPagingSource(private val videoItems: List<VideoItem>): PagingSource<Long, VideoItem>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoItem> {
        return LoadResult.Page(
            data = videoItems,
            prevKey = null,
            nextKey = null
        )
    }
}