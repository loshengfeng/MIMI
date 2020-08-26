package com.dabenxiang.mimi.view.myfollow

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MemberFollowListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: MyFollowPagingCallback
) : PageKeyedDataSource<Long, MemberFollowItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, MemberFollowItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMyMemberFollow("0", PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                emit(result.body())
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .filterNotNull()
                .collect { item ->
                    val members = item.content
                    val nextPageKey = when {
                        hasNextPage(
                            item.paging.count,
                            item.paging.offset,
                            members?.size ?: 0
                        ) -> PER_LIMIT_LONG
                        else -> null
                    }
                    pagingCallback.onTotalCount(item.paging.count, true)
                    val idList = ArrayList<Long>()
                    members?.forEach {
                        idList.add(it.userId)
                    }
                    pagingCallback.onIdList(idList, true)
                    callback.onResult(members ?: listOf(), null, nextPageKey)
                }
        }
    }

    override fun loadBefore(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, MemberFollowItem>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, MemberFollowItem>
    ) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getMyMemberFollow(next.toString(), PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                emit(result.body())
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .filterNotNull()
                .collect { item ->
                    val members = item.content
                    val nextPageKey = when {
                        hasNextPage(
                            item.paging.count,
                            item.paging.offset,
                            members?.size ?: 0
                        ) -> next + PER_LIMIT_LONG
                        else -> null
                    }
                    pagingCallback.onTotalCount(item.paging.count, false)
                    val idList = ArrayList<Long>()
                    members?.forEach {
                        idList.add(it.userId)
                    }
                    pagingCallback.onIdList(idList, false)
                    callback.onResult(members ?: listOf(), nextPageKey)
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