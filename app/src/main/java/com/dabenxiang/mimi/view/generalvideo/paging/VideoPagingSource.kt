package com.dabenxiang.mimi.view.generalvideo.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import kotlin.math.ceil

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
                    orderByType = orderByType,
                    category = category,
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

            val itemsWithAd = arrayListOf<StatisticsItem>()
            if (needAd) {
                if (lastId == 0L) {
                    val topAdItem =
                        domainManager.getAdRepository()
                            .getAD("${getAdCode()}_top", adWidth, adHeight)
                            .body()?.content?.get(0)?.ad?.first() ?: AdItem()
                    itemsWithAd.add(StatisticsItem(adItem = topAdItem))
                }
                val adCount = ceil((items?.size ?: 0).toFloat() / 10).toInt()
                val adItems =
                    domainManager.getAdRepository()
                        .getAD(getAdCode(), adWidth, adHeight, adCount)
                        .body()?.content?.get(0)?.ad ?: arrayListOf()
                items?.forEachIndexed { index, item ->
                    itemsWithAd.add(item)
                    if (index % 10 == 9) itemsWithAd.add(getAdItem(adItems))
                }
                if ((items?.size ?: 0) % 10 != 0) itemsWithAd.add(getAdItem(adItems))
            }

            val nextOffset = when {
                lastId != lastItem?.id && items?.isNotEmpty() == true -> lastItem?.id
                else -> null
            }

            val data = if (nextOffset != null) {
                if (needAd) itemsWithAd else items ?: arrayListOf()
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
        return if (isCategoryPage) "categorie"
        else when (category) {
            "国产" -> "categorie1"
            "日韩" -> "categorie2"
            "动漫" -> "categorie3"
            "无码" -> "categorie4"
            else -> "categorie"
        }
    }

    private fun getAdItem(adItems: ArrayList<AdItem>): StatisticsItem {
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems.removeFirst()
        return StatisticsItem(adItem = adItem)
    }
}