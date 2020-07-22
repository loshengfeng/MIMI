package com.dabenxiang.mimi.view.search.post

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchPostDataSource(
    private val pagingCallback: PagingCallback,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val type: PostType = PostType.TEXT,
    private val tag: String = ""
) : PageKeyedDataSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, MemberPostItem>
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.searchPost(
                    type,
                    tag,
                    0,
                    PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()
                val memberPostItems = body?.content
                val nextPageKey = when {
                    hasNextPage(
                        body?.paging?.count ?: 0,
                        body?.paging?.offset ?: 0,
                        memberPostItems?.size ?: 0
                    ) -> PER_LIMIT
                    else -> null
                }
                emit(InitResult(memberPostItems ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    pagingCallback.onSucceed()
                    callback.onResult(it.list, null, it.nextKey)
                }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MemberPostItem>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MemberPostItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.searchPost(
                    type,
                    tag,
                    next,
                    PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    it.body()?.also { item ->
                        item.content?.also { list ->
                            val nextPageKey = when {
                                hasNextPage(
                                    item.paging.count,
                                    item.paging.offset,
                                    list.size
                                ) -> next + PER_LIMIT
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
            currentSize < PostFollowDataSource.PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

    private data class InitResult(val list: List<MemberPostItem>, val nextKey: Int?)

}