package com.dabenxiang.mimi.view.order

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.OrderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class OrderListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val type: OrderType? = null
) : PageKeyedDataSource<Long, OrderItem>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<OrderItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, OrderItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = type?.let {
                    domainManager.getApiRepository().getOrderByType(it, "0", PER_LIMIT)
                } ?: let { domainManager.getApiRepository().getOrder("0", PER_LIMIT) }
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val clubs = item?.content?.orders

                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        clubs?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }
                emit(InitResult(clubs ?: arrayListOf(), nextPageKey))

                if (type == null) {
                    pagingCallback.onGetAny(item?.content?.balance)
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, OrderItem>) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, OrderItem>) {
        Timber.d("loadAfter")
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = type?.let {
                    domainManager.getApiRepository().getOrderByType(it, next.toString(), PER_LIMIT)
                } ?: let { domainManager.getApiRepository().getOrder(next.toString(), PER_LIMIT) }
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    response.body()?.also { item ->
                        val nextPageKey = when {
                            hasNextPage(
                                item.paging.count,
                                item.paging.offset,
                                0
                            ) -> next + PER_LIMIT_LONG
                            else -> null
                        }
                        callback.onResult(item.content?.orders ?: arrayListOf(), nextPageKey)
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