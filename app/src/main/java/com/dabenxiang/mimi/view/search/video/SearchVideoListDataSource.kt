package com.dabenxiang.mimi.view.search.video

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class SearchVideoListDataSource(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: SearchPagingCallback,
    private val isAdult: Boolean = false,
    private val category: String = "",
    private val tag: String = "",
    private val name: String = "",
    private val adHeight: Int,
    private val adWidth: Int
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
                val adRepository = domainManager.getAdRepository()
                val adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()

                val result = domainManager.getApiRepository().searchHomeVideos(
                    q = name,
                    isAdult = true,
                    offset = "0",
                    limit = PER_LIMIT,
                    tag = tag,
                    category = category
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

                val list = mutableListOf<VideoItem>()
                videos?.forEachIndexed { index, videoItem ->
                    if (index % 2 == 0 && index != 0) {
                        val item = VideoItem(type = PostType.AD, adItem = adItem)
                        list.add(item)
                    }
                    list.add(videoItem)
                }
                emit(Pair(LoadResult(list, nextPageKey), totalCount))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
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
        val next = params.key

        viewModelScope.launch {
            flow {
                val adRepository = domainManager.getAdRepository()
                val adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()

                val result = domainManager.getApiRepository().searchHomeVideos(
                    q = name,
                    isAdult = true,
                    offset = next.toString(),
                    limit = PER_LIMIT,
                    tag = tag,
                    category = category
                )
                if (!result.isSuccessful) throw HttpException(result)

                val body = result.body()
                val videos = body?.content?.videos
                videos?.let { adjustData(it) }
                val nextPageKey = when {
                    hasNextPage(
                        body?.paging?.count ?: 0,
                        body?.paging?.offset ?: 0,
                        videos?.size ?: 0
                    ) -> next + (videos?.size ?: 0)
                    else -> null
                }

                val list = mutableListOf<VideoItem>()
                videos?.forEachIndexed { index, videoItem ->
                    if (index % 2 == 0) {
                        val item = VideoItem(type = PostType.AD, adItem = adItem)
                        list.add(item)
                    }
                    list.add(videoItem)
                }

                emit(Pair(list, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    pagingCallback.onSucceed()
                    callback.onResult(it.first, it.second)
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