package com.dabenxiang.mimi.view.ranking

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class RankingDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val statisticsType: StatisticsType,
    private val postType: PostType
) : PageKeyedDataSource<Long, PostStatisticsItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<PostStatisticsItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, PostStatisticsItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .getRankingList(
                        statisticsType = statisticsType,
                        postType = postType,
                        offset = "0",
                        limit = PER_LIMIT
                    )
                if (!result.isSuccessful) throw HttpException(result)
                val items = result.body()?.content
                emit(InitResult(items ?: arrayListOf(), null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, PostStatisticsItem>
    ) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, PostStatisticsItem>
    ) {
        Timber.d("loadAfter")
    }
}