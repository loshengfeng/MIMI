package com.dabenxiang.mimi.view.search.post.tag

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchPostByTagDataSource(
    private val pagingCallback: SearchPagingCallback,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val type: PostType = PostType.TEXT,
    private val tag: String = "",
    private val isPostFollow: Boolean,
    private val adWidth: Int,
    private val adHeight: Int
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
                val adRepository = domainManager.getAdRepository()
                val adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()

                val apiRepository = domainManager.getApiRepository()
                val result = if (isPostFollow) {
                    apiRepository.searchPostFollowByTag(tag, 0, PER_LIMIT)
                } else {
                    apiRepository.searchPostByTag(type, tag, 0, PER_LIMIT)
                }
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
                val count = body?.paging?.count ?: 0
                pagingCallback.onTotalCount(count)

                val list = mutableListOf<MemberPostItem>()
                memberPostItems?.forEachIndexed { index, memberPostItem ->
                    if (index % 2 == 0 && index != 0) {
                        val item = MemberPostItem(type = PostType.AD, adItem = adItem)
                        list.add(item)
                    }
                    list.add(memberPostItem)
                }

                emit(SearchResult(list, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    pagingCallback.onCurrentItemCount(it.list.size.toLong(), true)
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
                val adRepository = domainManager.getAdRepository()
                val adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()

                val apiRepository = domainManager.getApiRepository()
                val result = if (isPostFollow) {
                    apiRepository.searchPostFollowByTag(tag, next, PER_LIMIT)
                } else {
                    apiRepository.searchPostByTag(type, tag, next, PER_LIMIT)
                }
                if (!result.isSuccessful) throw HttpException(result)

                val body = result.body()
                val memberPostItems = body?.content

                val nextPageKey = when {
                    hasNextPage(
                        body?.paging?.count ?: 0,
                        body?.paging?.offset ?: 0,
                        memberPostItems?.size ?: 0
                    ) -> next + PER_LIMIT
                    else -> null
                }

                val list = mutableListOf<MemberPostItem>()
                memberPostItems?.forEachIndexed { index, memberPostItem ->
                    if (index % 2 == 0) {
                        val item = MemberPostItem(type = PostType.AD, adItem = adItem)
                        list.add(item)
                    }
                    list.add(memberPostItem)
                }

                emit(SearchResult(list, nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect {
                    pagingCallback.onCurrentItemCount(it.list.size.toLong(), false)
                    callback.onResult(it.list, it.nextKey)
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

    private data class SearchResult(val list: List<MemberPostItem>, val nextKey: Int?)

}