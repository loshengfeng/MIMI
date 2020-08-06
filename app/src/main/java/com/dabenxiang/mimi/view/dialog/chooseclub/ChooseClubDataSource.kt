package com.dabenxiang.mimi.view.dialog.chooseclub

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ChooseClubDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback
) : PageKeyedDataSource<Long, Any>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<Any>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, Any>
    ) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClubPost(
                    offset = 0,
                    limit = PER_LIMIT.toInt()
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                val item = resp.body()
                val clubItems = item?.content

                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        clubItems?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }

                Timber.d("loadInitial_nextPageKey: ${nextPageKey.toString()}")

                emit(InitResult(clubItems ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> Timber.d("e: $e") }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
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
                val resp = domainManager.getApiRepository().getMembersClubPost(
                    offset = next.toInt(),
                    limit = PER_LIMIT.toInt()
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(resp)
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> Timber.d("e: $e") }
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
                            callback.onResult(list as List<Any>, nextPageKey)
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