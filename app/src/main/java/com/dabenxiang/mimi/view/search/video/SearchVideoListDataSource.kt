package com.dabenxiang.mimi.view.search.video

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.SearchVideoPagingCallback
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class SearchVideoListDataSource(
        private val viewModelScope: CoroutineScope,
        private val domainManager: DomainManager,
        private val pagingCallback: SearchVideoPagingCallback,
        private val isAdult: Boolean = false,
        private val tag: String = "",
        private val name: String = ""
) : PageKeyedDataSource<Long, VideoItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class LoadResult(val list: List<VideoItem>, val nextKey: Long?)

    override fun loadInitial(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<Long, VideoItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().searchHomeVideos(
                        q = name,
                        isAdult = isAdult,
                        offset = "0",
                        limit = PER_LIMIT,
                        tag = tag
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val videos = item?.content?.videos
                videos?.let { adjustData(it) }
                val totalCount = item?.paging?.count ?: 0
                pagingCallback.onTotalCount(totalCount)
                val nextPageKey = when {
                    hasNextPage(
                            totalCount,
                            item?.paging?.offset ?: 0,
                            videos?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }
                emit(Pair<LoadResult, Long>(
                    LoadResult(
                        videos
                            ?: ArrayList(), nextPageKey
                    ), totalCount))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        pagingCallback.onThrowable(e)
                    }
                    .onCompletion { pagingCallback.onLoaded() }
                    .collect {
                        pagingCallback.onSucceed()
                        it.first.run { callback.onResult(list, null, nextKey) }
                    }

        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, VideoItem>) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(
            params: LoadParams<Long>,
            callback: LoadCallback<Long, VideoItem>
    ) {
        Timber.d("loadAfter")
        val next = params.key

        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().searchHomeVideos(
                        q = name,
                        isAdult = isAdult,
                        offset = next.toString(),
                        limit = PER_LIMIT,
                        tag = tag
                )
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        pagingCallback.onThrowable(e)
                    }
                    .onCompletion { pagingCallback.onLoaded() }
                    .collect {
                        pagingCallback.onSucceed()
                        it.body()?.run {
                            content?.run {
                                val nextPageKey = when {
                                    hasNextPage(
                                            paging.count ?: 0,
                                            paging.offset ?: 0,
                                            videos?.size ?: 0
                                    ) -> next + (videos?.size ?: 0)
                                    else -> null
                                }

                                if (videos != null) {
                                    adjustData(videos)
                                    callback.onResult(videos, nextPageKey)
                                }
                            }
                        }
                    }
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }

    private fun adjustData(list: List<VideoItem>) {
        list.forEach { videoItem ->
            videoItem.isAdult = isAdult
            videoItem.searchingTag = tag
            videoItem.searchingStr = name
        }
    }
}