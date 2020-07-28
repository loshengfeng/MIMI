package com.dabenxiang.mimi.view.home.club

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.callback.PostPagingCallBack
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClubDataSource (
    private val pagingCallback: PostPagingCallBack,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val adWidth: Int,
    private val adHeight: Int
) : PageKeyedDataSource<Int, MemberClubItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, MemberClubItem>
    ) {
        viewModelScope.launch {
            flow {
                val adRepository = domainManager.getAdRepository()
                val adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()
                pagingCallback.onGetAd(adItem)

                val result = domainManager.getApiRepository().getMembersClubPost(
                    offset = 0,
                    limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()
                val memberClubItems = body?.content

                val nextPageKey = when {
                    hasNextPage(
                        body?.paging?.count ?: 0,
                        body?.paging?.offset ?: 0,
                        memberClubItems?.size ?: 0
                    ) -> MemberPostDataSource.PER_LIMIT
                    else -> null
                }
                emit(Pair(memberClubItems ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { (items, nextKey) ->
                    pagingCallback.onSucceed()
                    callback.onResult(items, null, nextKey)
                }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MemberClubItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMembersClubPost(
                    offset = next,
                    limit = PER_LIMIT
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

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MemberClubItem>) {
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < MemberPostDataSource.PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

}