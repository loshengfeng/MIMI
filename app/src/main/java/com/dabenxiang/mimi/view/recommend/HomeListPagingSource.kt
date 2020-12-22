package com.dabenxiang.mimi.view.recommend

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.HomeListItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import kotlin.math.ceil

class HomeListPagingSource(
    private val domainManager: DomainManager,
    private val adWidth: Int,
    private val adHeight: Int,
    private val needAd: Boolean = true
): PagingSource<Long, HomeListItem>() {
    companion object {
        const val PER_LIMIT = 30
    }
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, HomeListItem> {
        return try {
            val offset = params.key ?: 0L
            val result = domainManager.getApiRepository().getHomeList()

            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()

            val homeList = item?.content

            val nextOffset = when {
                hasNextPage(
                    item?.paging?.count ?: 0,
                    item?.paging?.offset ?: 0,
                    homeList?.size ?: 0
                ) -> offset + PER_LIMIT
                else -> null
            }

            val adCount = ceil((homeList?.size ?: 0).toFloat() / 2).toInt()
            val adItems =
                domainManager.getAdRepository().getAD("home", adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()
            val resultList: ArrayList<HomeListItem> = arrayListOf()
            homeList?.takeIf { needAd && homeList.isNotEmpty() }?.forEachIndexed { index, homeListItem ->
                resultList.add(homeListItem)
                if (index % 2 == 1) resultList.add(getAdItem(adItems))
            }
            if ((homeList?.size ?: 0) % 2 != 0) resultList.add(getAdItem(adItems))

            LoadResult.Page(
                data = resultList,
                prevKey = if (offset == 0L) null else offset - PER_LIMIT,
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int = PER_LIMIT): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }

    private fun getAdItem(adItems: ArrayList<AdItem>): HomeListItem {
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems.removeFirst()
        return HomeListItem(adItem = adItem)
    }
}