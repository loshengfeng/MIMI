package com.dabenxiang.mimi.view.ranking

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class RankingVideosDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val statisticsType: StatisticsType
) : PageKeyedDataSource<Long, StatisticsItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<StatisticsItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, StatisticsItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().statisticsHomeVideos(
                    statisticsType=statisticsType,
                    isAdult = true,
                    offset = 0,
                    limit = PER_LIMIT_LONG.toInt()
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val clubs = item?.content

                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        clubs?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }
                emit(InitResult(clubs ?: arrayListOf(), nextPageKey))

            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, StatisticsItem>) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, StatisticsItem>) {
        Timber.d("loadAfter")
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().statisticsHomeVideos(
                    statisticsType=statisticsType,
                    isAdult = true,
                    offset = 0,
                    limit = PER_LIMIT_LONG.toInt()
                )
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    response.body()?.also { item ->
                        item.content?.also { list ->
                            val nextPageKey = when {
                                hasNextPage(
                                    item.paging.count,
                                    item.paging.offset,
                                    list.size
                                ) -> next + PER_LIMIT_LONG
                                else -> null
                            }

                            callback.onResult(list, nextPageKey)
                        }
                    }
                }
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        // Spec: Only show 10 data
        return false
    }
}