package com.dabenxiang.mimi.view.clip

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
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
                offset = offset.toString(),
                limit = PER_LIMIT.toString()
            )
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()

            val videos = item?.content?.videos
            val nextOffset = when {
                hasNextPage(
                    item?.paging?.count ?: 0,
                    item?.paging?.offset ?: 0,
                    videos?.size ?: 0
                ) -> offset + PER_LIMIT
                else -> null
            }

            LoadResult.Page(
                data = videos ?: arrayListOf(),
                prevKey = if (offset == 0L) null else offset - PER_LIMIT,
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}