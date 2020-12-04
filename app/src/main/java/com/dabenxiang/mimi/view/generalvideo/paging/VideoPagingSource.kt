package com.dabenxiang.mimi.view.generalvideo.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber

class VideoPagingSource(
    private val domainManager: DomainManager,
    private val category: String?,
    private val orderByType: Int = StatisticsOrderType.LATEST.value,
    private val adWidth: Int,
    private val adHeight: Int,
    private val needAd: Boolean
) : PagingSource<Long, StatisticsItem>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, StatisticsItem> {
        return try {
            val lastId = params.key ?: 0L

            val result = domainManager.getApiRepository()
                .statisticsHomeVideos(
                    startTime = "",
                    endTime = "",
                    orderByType = orderByType,
                    category = category,
                    offset = 0,
                    limit = params.loadSize,
                    lastId = lastId
                )
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val items = body?.content

            val lastItem = if (items?.isNotEmpty() == true) {
                items.last()
            } else {
                null
            }

            if (needAd) {
                val adItem = domainManager.getAdRepository()
                    .getAD(adWidth, adHeight).body()?.content ?: AdItem()
                items?.add(0, StatisticsItem(adItem = adItem))
            }

            val nextOffset = when {
                lastId != lastItem?.id && items?.isNotEmpty() == true -> lastItem?.id
                else -> null
            }

            return LoadResult.Page(
                data = items ?: arrayListOf(),
                prevKey = null,
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            Timber.e(exception)
            LoadResult.Error(exception)
        }
    }
}