package com.dabenxiang.mimi.view.home

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.simpleVideoItemToVideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class GuessLikeDataSource(
    private val isAdult: Boolean,
    private val category: String,
    private val viewModelScope: CoroutineScope,
    private val apiRepository: ApiRepository,
    private val pagingCallback: GuessLikePagingCallBack
) : PageKeyedDataSource<Long, BaseVideoItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class EmitResult(val list: List<BaseVideoItem>, val nextKey: Long?)

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, BaseVideoItem>) {
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()

                val result = apiRepository.searchWithCategory(category, isAdult, "0", PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)

                val item = result.body()
                val videos = item?.content
                if (videos != null) {
                    returnList.addAll(videos.simpleVideoItemToVideoItem(isAdult))
                }

                val nextPageKey = when {
                    hasNextPage(item?.paging?.count ?: 0, item?.paging?.offset ?: 0, videos?.size ?: 0) -> PER_LIMIT_LONG
                    else -> null
                }

                emit(EmitResult(returnList, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion {
                    pagingCallback.onLoaded()
                }.collect { response ->
                    pagingCallback.onLoadInit(response.list.size)
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, BaseVideoItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = apiRepository.searchWithCategory(category, isAdult, next.toString(), PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)

                result.body()?.also { item ->
                    item.content?.also { list ->
                        val nextPageKey = when {
                            hasNextPage(item.paging.count, item.paging.offset, list.size) -> next + PER_LIMIT_LONG
                            else -> null
                        }

                        emit(EmitResult(list.simpleVideoItemToVideoItem(isAdult), nextPageKey))
                    }
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    pagingCallback.onThrowable(e)
                }.onCompletion {
                    pagingCallback.onLoaded()
                }.collect { response ->
                    callback.onResult(response.list, response.nextKey)
                }
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