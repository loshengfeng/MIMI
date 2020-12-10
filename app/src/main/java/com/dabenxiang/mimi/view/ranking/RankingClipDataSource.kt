package com.dabenxiang.mimi.view.ranking

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RankingClipDataSource(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val startTime:String,
    private val endTime:String
) : PageKeyedDataSource<Long, VideoItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<VideoItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, VideoItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().searchShortVideo(
                    startTime = startTime,
                    endTime = endTime,
                    orderByType = StatisticsOrderType.HOTTEST,
                    offset = "0",
                    limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()

                val videos = item?.content?.videos
                emit(InitResult(videos ?: arrayListOf(), null))
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

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, VideoItem>) {

    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, VideoItem>) {

    }

}