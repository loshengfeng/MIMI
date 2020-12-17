package com.dabenxiang.mimi.view.generalvideo.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
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
    private val needAd: Boolean,
    private val isCategoryPage: Boolean = false
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
                    limit = NETWORK_PAGE_SIZE,
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

            if (needAd && lastId == 0L) {
                val adItems =
                    domainManager.getAdRepository().getAD(getAdCode(), adWidth, adHeight, 1)
                        .body()?.content?.get(0)?.ad ?: arrayListOf()
                val adItem = if (adItems.isEmpty()) AdItem() else adItems.first()
                items?.add(0, StatisticsItem(adItem = adItem))
            }

            val nextOffset = when {
                lastId != lastItem?.id && items?.isNotEmpty() == true -> lastItem?.id
                else -> null
            }

            val data = if (nextOffset != null) {
                items ?: arrayListOf()
            } else {
                emptyList()
            }

            return LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = nextOffset
            )

        } catch (exception: Exception) {
            Timber.e(exception)
            LoadResult.Error(exception)
        }
    }

    private fun getAdCode(): String {
        return if (isCategoryPage) "categorie_top"
        else when (category) {
            "国产" -> "categorie1_top"
            "日韩" -> "categorie2_top"
            "动漫" -> "categorie3_top"
            "无码" -> "categorie4_top"
            else -> "categorie_top"
        }
    }
}