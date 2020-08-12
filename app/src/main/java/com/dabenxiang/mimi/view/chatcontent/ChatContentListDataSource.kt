package com.dabenxiang.mimi.view.chatcontent

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatContentListDataSource(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val chatId: Long,
    private val pagingCallback: PagingCallback
) : PageKeyedDataSource<Long, ChatContentItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<ChatContentItem>, val nextKey: Long?)

    override fun loadInitial(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<Long, ChatContentItem>
    ) {
//        viewModelScope.launch {
//            flow {
//                val result = domainManager.getApiRepository().getMessage(
//                        chatId,
//                        offset = "0",
//                        limit = PER_LIMIT
//                )
//                if (!result.isSuccessful) throw HttpException(result)
//                val item = result.body()
//                val messages = item?.content
//                val totalCount = item?.paging?.count ?: 0
//                val nextPageKey = when {
//                    hasNextPage(
//                            totalCount,
//                            item?.paging?.offset ?: 0,
//                            messages?.size ?: 0
//                    ) -> PER_LIMIT_LONG
//                    else -> null
//                }
//                emit(
//                        InitResult(messages ?: ArrayList(), nextPageKey)
//                )
//            }
//                    .flowOn(Dispatchers.IO)
//                    .onStart { pagingCallback.onLoading() }
//                    .catch { e -> pagingCallback.onThrowable(e) }
//                    .onCompletion { pagingCallback.onLoaded() }
//                    .collect { response ->
//                        pagingCallback.onSucceed()
//                        val result = adjustData(response.list)
//                        pagingCallback.onTotalCount(result.size.toLong())
////                        callback.onResult(result, null, response.nextKey)
//                    }
//
//        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, ChatContentItem>) {
        Timber.d("loadBefore")
    }

    override fun loadAfter(
            params: LoadParams<Long>,
            callback: LoadCallback<Long, ChatContentItem>
    ) {
//        Timber.d("loadAfter")
//        val next = params.key
//
//        viewModelScope.launch {
//            flow {
//                val result = domainManager.getApiRepository().getMessage(
//                        chatId,
//                        offset = next.toString(),
//                        limit = PER_LIMIT
//                )
//                if (!result.isSuccessful) throw HttpException(result)
//                emit(result)
//            }
//                    .flowOn(Dispatchers.IO)
//                    .catch { e ->
//                        pagingCallback.onThrowable(e)
//                    }
//                    .onCompletion { pagingCallback.onLoaded() }
//                    .collect {
//                        pagingCallback.onSucceed()
//                        it.body()?.run {
//                            Timber.d("neo,content = ${content?.size}")
//                            content?.run {
//                                val nextPageKey = when {
//                                    hasNextPage(
//                                            paging.count,
//                                            paging.offset,
//                                            size
//                                    ) -> next + (size)
//                                    else -> null
//                                }
//                                val result = adjustData(content)
//                                pagingCallback.onTotalCount(result.size.toLong())
//                                callback.onResult(result, nextPageKey)
//                            }
//                        }
//                    }
//        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }

    private fun adjustData(list: List<ChatContentItem>): List<ChatContentItem> {
        val result: ArrayList<ChatContentItem> = ArrayList()
        var lastDate: String = ""
        for (i: Int in list.indices) {
            val item = list[i]
            item.payload?.sendTime?.let { date ->
                val currentDate = SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date)
                if (lastDate.isNotEmpty() && lastDate != currentDate) {
                    result.add(ChatContentItem(dateTitle = lastDate))
                }
                result.add(item)
                lastDate = currentDate
                if (i == list.size - 1) {
                    result.add(ChatContentItem(dateTitle = lastDate))
                }
            }
        }
        return result
    }
}