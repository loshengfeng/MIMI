package com.dabenxiang.mimi.view.club.post

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException

class ClubPicListDataSource(
        private val domainManager: DomainManager,
        private val pagingCallback: PagingCallback,
        private val viewModelScope: CoroutineScope,
        private val adWidth: Int,
        private val adHeight: Int
) : PageKeyedDataSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 3
    }


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, MemberPostItem>) {
        viewModelScope.launch {
            flow {
                val adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content
                        ?: AdItem()

                val result = domainManager.getApiRepository().getMembersPost(PostType.IMAGE, OrderBy.NEWEST,
                        0, PER_LIMIT
                )

                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()
                val postItem = body?.content
                postItem?.forEachWithIndex { i, memberPostItem ->
                    if (i % AD_GAP == 0)
                        postItem.add(i, MemberPostItem(type = PostType.AD, adItem = adItem))
                }

                val nextPageKey = when {
                    hasNextPage(
                            body?.paging?.count ?: 0,
                            body?.paging?.offset ?: 0,
                            postItem?.size ?: 0
                    ) -> PER_LIMIT
                    else -> null
                }
                emit(InitResult(postItem ?: arrayListOf(), nextPageKey))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { pagingCallback.onLoading() }
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
                val result = domainManager.getApiRepository().getMembersPost(PostType.IMAGE, OrderBy.HOTTEST, offset = next.toInt(), limit = PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { pagingCallback.onLoading() }
                    .catch { e -> pagingCallback.onThrowable(e) }
                    .onCompletion { pagingCallback.onLoaded() }
                    .collect {
                        it.body()?.also { item ->
                            item.content?.also { list ->
                                pagingCallback.onCurrentItemCount(list.size.toLong(), false)
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
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

    private data class InitResult(val list: List<MemberPostItem>, val nextKey: Int?)
}