package com.dabenxiang.mimi.view.clip

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.api.vo.VideoSearchItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import retrofit2.HttpException

class ClipPagingSource(
    private val domainManager: DomainManager,
    private val orderByType: StatisticsOrderType
) : PagingSource<Long, VideoItem>() {
    companion object {
        const val PER_LIMIT = 20
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoItem> {
        return try {
            val offset = params.key ?: 0L
            val result = domainManager.getApiRepository().searchShortVideo(
                orderByType = orderByType,
                offset = 0.toString(),
                limit = PER_LIMIT.toString()
            )
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()
            val videos = item?.content?.videos
            val nextOffset = when {
                hasNextPage(
                    item?.paging?.count ?: 0,
                    item?.paging?.offset ?: 0,
                    videos?.size ?: 0,
                    params.loadSize
                ) -> offset + params.loadSize
                else -> null
            }

            LoadResult.Page(
                data = videos ?: arrayListOf(),
                prevKey = if (offset == 0L) null else offset - params.loadSize.toLong(),
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }
}