package com.dabenxiang.mimi.view.home.club

import android.text.TextUtils
import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubDataSource(
    private val pagingCallback: PagingCallback,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val adWidth: Int,
    private val adHeight: Int,
    private val keyword: String = ""
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

                val result = if (TextUtils.isEmpty(keyword)) {
                    domainManager.getApiRepository().getMembersClubPost(
                        offset = 0,
                        limit = PER_LIMIT
                    )
                } else {
                    domainManager.getApiRepository().getMembersClubPost(
                        offset = 0,
                        limit = PER_LIMIT,
                        keyword = keyword
                    )
                }
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
                pagingCallback.onTotalCount(body?.paging?.count ?: 0)

                if (!TextUtils.isEmpty(keyword)) {
                    val list = mutableListOf<MemberClubItem>()
                    memberClubItems?.forEachIndexed { index, memberClubItem ->
                        if (index % 2 == 0 && index != 0) {
                            val item = MemberClubItem(type = PostType.AD, adItem = adItem)
                            list.add(item)
                        }
                        list.add(memberClubItem)
                    }
                    emit(Pair(list, nextPageKey))
                } else {
                    memberClubItems?.add(0, MemberClubItem(type = PostType.AD, adItem = adItem))
                    emit(Pair(memberClubItems, nextPageKey))
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { (items, nextKey) ->
                    callback.onResult(items ?: arrayListOf(), null, nextKey)
                }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MemberClubItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                var adItem: AdItem? = null

                val result = if (TextUtils.isEmpty(keyword)) {
                    domainManager.getApiRepository().getMembersClubPost(
                        offset = next,
                        limit = PER_LIMIT
                    )
                } else {
                    val adRepository = domainManager.getAdRepository()
                    adItem = adRepository.getAD(adWidth, adHeight).body()?.content ?: AdItem()
                    domainManager.getApiRepository().getMembersClubPost(
                        offset = next,
                        limit = PER_LIMIT,
                        keyword = keyword
                    )
                }
                if (!result.isSuccessful) throw HttpException(result)

                val body = result.body()
                val memberClubItems = body?.content

                val nextPageKey = when {
                    hasNextPage(
                        body?.paging?.count ?: 0,
                        body?.paging?.offset ?: 0,
                        memberClubItems?.size ?: 0
                    ) -> next + PER_LIMIT
                    else -> null
                }

                if (!TextUtils.isEmpty(keyword)) {
                    val list = mutableListOf<MemberClubItem>()
                    memberClubItems?.forEachIndexed { index, memberClubItem ->
                        if (index % 2 == 0) {
                            val item = MemberClubItem(type = PostType.AD, adItem = adItem)
                            list.add(item)
                        }
                        list.add(memberClubItem)
                    }
                    emit(Pair(list, nextPageKey))
                } else {
                    emit(Pair(memberClubItems, nextPageKey))
                }
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { (items, nextKey) ->
                    callback.onResult(items!!, nextKey)
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