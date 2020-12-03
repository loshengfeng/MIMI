package com.dabenxiang.mimi.view.ranking

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class RankingClipPagingSource(
    private val domainManager: DomainManager,
    private val startTime:String,
    private val endTime:String
) : PagingSource<Long, VideoItem>() {
    companion object {
        const val PER_LIMIT = 10
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoItem> {
        return try {
            val offset = params.key ?: 0L
            val result = domainManager.getApiRepository().searchShortVideo(
                startTime = startTime,
                endTime = endTime,
                orderByType = StatisticsOrderType.HOTTEST,
                offset = offset.toString(),
                limit = PER_LIMIT.toString()
            )
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()

            val videos = item?.content?.videos

            LoadResult.Page(
                data = videos ?: arrayListOf(),
                prevKey = null,
                nextKey = null
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}