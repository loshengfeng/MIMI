package com.dabenxiang.mimi.view.favroite

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.FavoritePagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class FavoritePostListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: FavoritePagingCallback
) : PageKeyedDataSource<Long, Any>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<Any>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, Any>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getPostFavorite("0", PER_LIMIT)
                Timber.d("over")
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val playListItems = item?.content

                pagingCallback.onReceiveResponse(playListItems as ArrayList<PostFavoriteItem>)

                val nextPageKey = when {
                    hasNextPage(
                        item.paging.count,
                        item.paging.offset,
                        playListItems.size
                    ) -> PER_LIMIT_LONG
                    else -> null
                }

                Timber.d("loadInitial_nextPageKey: ${nextPageKey.toString()}")

                emit(InitResult(playListItems ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> Timber.d("e: $e") }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    pagingCallback.onTotalCount(response.list.size)
                    val ids = ArrayList<Long>()
                    response.list.forEach {
                        (it as PostFavoriteItem).id?.let { it1 -> ids.add(it1) }
                    }
                    pagingCallback.onTotalVideoId(ids,true)
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, Any>
    ) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, Any>
    ) {
        Timber.d("loadAfter")
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getPostFavorite(next.toString(), PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    response.body()?.also { item ->
                        item.content?.also { list ->

                            val ids = ArrayList<Long>()
                            list.forEach {
                                it.id?.let { it1 -> ids.add(it1) }
                            }
                            pagingCallback.onTotalVideoId(ids, false)
                            pagingCallback.onReceiveResponse(list as ArrayList<PostFavoriteItem>)
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
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }
}