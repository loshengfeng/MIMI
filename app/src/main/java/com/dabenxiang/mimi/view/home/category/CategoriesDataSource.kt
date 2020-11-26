package com.dabenxiang.mimi.view.home.category

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.model.vo.statisticsItemToVideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoriesDataSource(
    private val category: String?,
    private val orderByType: Int,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val adWidth: Int,
    private val adHeight: Int
) : PageKeyedDataSource<Int, BaseVideoItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    private data class LoadResult(
        val list: List<BaseVideoItem>,
        val nextKey: Int?
    )

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, BaseVideoItem>
    ) {
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()
                val adResult = domainManager.getAdRepository().getAD(adWidth, adHeight)
                val adItem = adResult.body()?.content ?: AdItem()
                returnList.add(BaseVideoItem.Banner(adItem))

                val result = domainManager.getApiRepository().statisticsHomeVideos(
                    category = category,
                    orderByType = orderByType,
                    offset = 0,
                    limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val videos = item?.content
                pagingCallback.onTotalCount(item?.paging?.count?: 0)
                videos?.statisticsItemToVideoItem()?.let { returnList.addAll(it) }
                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        videos?.size ?: 0
                    ) -> PER_LIMIT
                    else -> null
                }
                emit(LoadResult(returnList, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    callback.onResult(it.list, null, it.nextKey)
                }
        }
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, BaseVideoItem>
    ) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()

                val result = domainManager.getApiRepository().statisticsHomeVideos(
                    category = category,
                    orderByType = orderByType,
                    offset = next,
                    limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val videos = item?.content
                videos?.statisticsItemToVideoItem()?.let { returnList.addAll(it) }
                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        videos?.size ?: 0
                    ) -> next + PER_LIMIT
                    else -> null
                }
                emit(
                    LoadResult(
                        returnList,
                        nextPageKey
                    )
                )

            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .collect { callback.onResult(it.list, it.nextKey) }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, BaseVideoItem>) {
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}