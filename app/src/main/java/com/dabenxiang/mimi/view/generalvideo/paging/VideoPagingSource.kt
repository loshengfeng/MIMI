package com.dabenxiang.mimi.view.generalvideo.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class VideoPagingSource(
    private val domainManager: DomainManager,
    private val category: String,
    private val adWidth: Int,
    private val adHeight: Int,
) : PagingSource<Long, StatisticsItem>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, StatisticsItem> {
        return try {
            val offset = params.key ?: 0L

            val result = domainManager.getApiRepository()
                .statisticsHomeVideos(
                    startTime = "",
                    endTime = "",
                    orderByType = StatisticsOrderType.LATEST.value,
                    category = category,
                    offset = offset.toString().toInt(),
                    limit = params.loadSize
                )
            if (!result.isSuccessful) throw HttpException(result)

            val adItem = domainManager.getAdRepository()
                .getAD(adWidth, adHeight).body()?.content ?: AdItem()

            val body = result.body()
            val items = body?.content
            items?.add(0, StatisticsItem(adItem = adItem))

            val nextOffset = when {
                hasNextPage(
                    body?.paging?.count ?: 0,
                    body?.paging?.offset ?: 0,
                    items?.size ?: 0,
                    params.loadSize
                ) -> offset + params.loadSize
                else -> null
            }

            LoadResult.Page(
                data = items ?: arrayListOf(),
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