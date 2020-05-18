package com.dabenxiang.mimi.view.home

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.vo.VideoSearchDetail
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VideoListDataSource(
    private val isAdult: Boolean,
    private val category: String,
    private val viewModelScope: CoroutineScope,
    private val apiRepository: ApiRepository,
    private val pagingCallback: PagingCallback
) : PageKeyedDataSource<Long, BaseVideoItem>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<BaseVideoItem>, val nextKey: Long?)

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, BaseVideoItem>) {
        viewModelScope.launch {
            flow {
                val returnList = mutableListOf<BaseVideoItem>()

                // TODO: 取得廣告
                val adBanner = "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcSd667siFJDLVZKf6mIzT86lWuspAhd40nq-2ACy5UpAQYJEWSM&usqp=CAU"
                returnList.add(BaseVideoItem.Banner(adBanner))

                val result = apiRepository.searchHomeVideos(category, "", "", null, isAdult, "0", PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)

                val item = result.body()
                val videos = item?.content?.videos
                if (videos != null) {
                    returnList.addAll(videos.parser())
                }

                val nextPageKey = when {
                    hasNextPage(item?.paging?.count ?: 0, item?.paging?.offset ?: 0, videos?.size ?: 0) -> PER_LIMIT_LONG
                    else -> null
                }

                emit(InitResult(returnList, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion {
                    pagingCallback.onLoaded()
                }.collect { response ->
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, BaseVideoItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = apiRepository.searchHomeVideos("", "", "", null, isAdult, next.toString(), PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    pagingCallback.onThrowable(e)
                }.onCompletion {
                    pagingCallback.onLoaded()
                }.collect { response ->
                    response.body()?.also { item ->
                        item.content?.videos?.also { list ->
                            val nextPageKey = when {
                                hasNextPage(item.paging.count, item.paging.offset, list.size) -> next + PER_LIMIT_LONG
                                else -> null
                            }

                            callback.onResult(list.parser(), nextPageKey)
                        }
                    }
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

    private fun List<VideoSearchDetail>.parser(): List<BaseVideoItem> {
        val result = mutableListOf<BaseVideoItem>()
        forEach { item ->
            val holderItem = BaseVideoItem.Video(title = item.title, imgUrl = item.cover, isAdult = isAdult, resolution = "", info = "")
            result.add(holderItem)
        }

        return result
    }
}