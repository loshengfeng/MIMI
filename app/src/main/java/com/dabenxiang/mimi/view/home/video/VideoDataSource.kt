package com.dabenxiang.mimi.view.home.video

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.searchItemToVideoItem
import com.dabenxiang.mimi.model.holder.simpleVideoItemToVideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VideoDataSource(
    private val isAdult: Boolean,
    private val category: String?,
    private val viewModelScope: CoroutineScope,
    private val apiRepository: ApiRepository,
    private val pagingCallback: PagingCallback
) : PageKeyedDataSource<Long, BaseVideoItem>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class LoadResult(val list: List<BaseVideoItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, BaseVideoItem>
    ) {
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()

                // TODO: 取得廣告
                val adBanner =
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcSd667siFJDLVZKf6mIzT86lWuspAhd40nq-2ACy5UpAQYJEWSM&usqp=CAU"
                returnList.add(BaseVideoItem.Banner(adBanner))

                if (category != null) {
                    val result = apiRepository.searchWithCategory(category, isAdult, "0",
                        PER_LIMIT
                    )
                    if (!result.isSuccessful) throw HttpException(result)

                    val item = result.body()
                    val videos = item?.content
                    if (videos != null) {
                        returnList.addAll(videos.simpleVideoItemToVideoItem(isAdult))
                    }

                    val nextPageKey = when {
                        hasNextPage(
                            item?.paging?.count ?: 0,
                            item?.paging?.offset ?: 0,
                            videos?.size ?: 0
                        ) -> PER_LIMIT_LONG
                        else -> null
                    }
                    emit(
                        LoadResult(
                            returnList,
                            nextPageKey
                        )
                    )
                } else {
                    val result = apiRepository.searchHomeVideos(
                        isAdult = isAdult,
                        offset = "0",
                        limit = PER_LIMIT
                    )
                    if (!result.isSuccessful) throw HttpException(result)
                    val item = result.body()
                    val videos = item?.content?.videos
                    if (videos != null) {
                        returnList.addAll(videos.searchItemToVideoItem(isAdult))
                    }
                    val nextPageKey = when {
                        hasNextPage(
                            item?.paging?.count ?: 0,
                            item?.paging?.offset ?: 0,
                            videos?.size ?: 0
                        ) -> PER_LIMIT_LONG
                        else -> null
                    }
                    emit(
                        LoadResult(
                            returnList,
                            nextPageKey
                        )
                    )
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { callback.onResult(it.list, null, it.nextKey) }
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, BaseVideoItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()
                if (category != null) {
                    val result = apiRepository.searchWithCategory(
                        category,
                        isAdult,
                        next.toString(),
                        PER_LIMIT
                    )
                    if (!result.isSuccessful) throw HttpException(result)
                    val item = result.body()
                    val videos = item?.content
                    if (videos != null) {
                        returnList.addAll(videos.simpleVideoItemToVideoItem(isAdult))
                    }
                    val nextPageKey = when {
                        hasNextPage(
                            item?.paging?.count ?: 0,
                            item?.paging?.offset ?: 0,
                            videos?.size ?: 0
                        ) -> next + PER_LIMIT_LONG
                        else -> null
                    }
                    emit(
                        LoadResult(
                            returnList,
                            nextPageKey
                        )
                    )
                } else {
                    val result = apiRepository.searchHomeVideos(
                        isAdult = isAdult,
                        offset = next.toString(),
                        limit = PER_LIMIT
                    )
                    if (!result.isSuccessful) throw HttpException(result)
                    val item = result.body()
                    val videos = item?.content?.videos
                    if (videos != null) {
                        returnList.addAll(videos.searchItemToVideoItem(isAdult))
                    }
                    val nextPageKey = when {
                        hasNextPage(
                            item?.paging?.count ?: 0,
                            item?.paging?.offset ?: 0,
                            videos?.size ?: 0
                        ) -> next + PER_LIMIT_LONG
                        else -> null
                    }
                    emit(
                        LoadResult(
                            returnList,
                            nextPageKey
                        )
                    )
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { callback.onResult(it.list, it.nextKey) }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, BaseVideoItem>) {

    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }
}