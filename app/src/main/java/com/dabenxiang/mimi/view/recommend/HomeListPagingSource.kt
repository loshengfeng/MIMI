package com.dabenxiang.mimi.view.recommend

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.HomeListItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class HomeListPagingSource(
    private val domainManager: DomainManager,
    private val adWidth: Int,
    private val adHeight: Int,
    private val needAd: Boolean = true
): PagingSource<Long, HomeListItem>() {
    companion object {
        const val PER_LIMIT = 20
    }
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, HomeListItem> {
        return try {
            val offset = params.key ?: 0L
            val result = domainManager.getApiRepository().getHomeList(
                offset = offset.toString(),
                limit = PER_LIMIT.toString()
            )

            val adItem = domainManager.getAdRepository()
                .getAD(adWidth, adHeight).body()?.content ?: AdItem()

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

            val resultList: ArrayList<HomeListItem> = arrayListOf()

            homeList?.takeIf { needAd && homeList.isNotEmpty() }?.forEachIndexed { index, homeListItem ->
                if (index % 2 == 0 && index != 0) {
                    resultList.add(HomeListItem(adItem = adItem))
                }
                resultList.add(homeListItem)
            }

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
}