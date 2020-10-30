package com.dabenxiang.mimi.view.inviteviprecord

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class InviteVipRecordListDataSource(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback
) : PageKeyedDataSource<Long, ReferrerHistoryItem>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<ReferrerHistoryItem>, val nextKey: Long?)


    override fun loadInitial(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<Long, ReferrerHistoryItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getReferrerHistory(
                        offset = "0",
                        limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val messages = item?.content
                val totalCount = item?.paging?.count ?: 0
                pagingCallback.onTotalCount(totalCount)
                val nextPageKey = when {
                    hasNextPage(
                            totalCount,
                            item?.paging?.offset ?: 0,
                            messages?.size?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }
                emit(InitResult(messages ?: ArrayList(), nextPageKey))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { pagingCallback.onLoading() }
                    .catch { e -> pagingCallback.onThrowable(e) }
                    .onCompletion { pagingCallback.onLoaded() }
                    .collect {response->
                        pagingCallback.onSucceed()
                        callback.onResult(response.list, null, response.nextKey)
                    }

        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, ReferrerHistoryItem>) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(
            params: LoadParams<Long>,
            callback: LoadCallback<Long, ReferrerHistoryItem>
    ) {
        Timber.d("loadAfter")
        val next = params.key

        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getReferrerHistory(
                        offset = next.toString(),
                        limit = PER_LIMIT
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
                                pagingCallback.onTotalCount(size.toLong())
                                val nextPageKey = when {
                                    hasNextPage(
                                            paging.count,
                                            paging.offset,
                                            size
                                    ) -> next + (size)
                                    else -> null
                                }

                                callback.onResult(content, nextPageKey)
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