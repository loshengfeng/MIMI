package com.dabenxiang.mimi.view.search.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SearchPostViewModel : ClubViewModel() {

    private val _searchTotalCount = MutableLiveData<Long>()
    val searchTotalCount: LiveData<Long> = _searchTotalCount

    var totalCount: Int = 0
    override val pagingCallback = object : SearchPagingCallback {
        override fun onTotalCount(count: Long) {
            _searchTotalCount.postValue(count)
        }
    }

    fun getSearchHistory(): ArrayList<String> {
        return pref.searchHistoryItem.searchHistory
    }

    fun clearSearchHistory() {
        pref.searchHistoryItem = SearchHistoryItem()
    }

    fun updateSearchHistory(keyword: String) {
        val searchHistoryItem = pref.searchHistoryItem
        if (!searchHistoryItem.searchHistory.contains(keyword)) {
            if (searchHistoryItem.searchHistory.size == 10) {
                searchHistoryItem.searchHistory.removeAt(0)
                searchHistoryItem.searchHistory.add(keyword)
            } else {
                searchHistoryItem.searchHistory.add(keyword)
            }
            pref.searchHistoryItem = searchHistoryItem
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(
        pageCode: String,
        type: PostType? = null,
        keyword: String? = null,
        tag: String? = null,
        orderBy: StatisticsOrderType? = null
    ) = clearResult(pageCode)
        .flatMapConcat {
            postItems(
                pageCode,
                type,
                keyword,
                tag,
                orderBy
            )
        }.cachedIn(viewModelScope)


    @OptIn(ExperimentalPagingApi::class)
    private fun postItems(
        pageCode: String,
        type: PostType? = null,
        keyword: String? = null,
        tag: String? = null,
        orderBy: StatisticsOrderType? = null
    ) = Pager(
        config = PagingConfig(pageSize = SearchPostMediator.PER_LIMIT),
        remoteMediator = SearchPostMediator(
            mimiDB,
            domainManager,
            adWidth,
            adHeight,
            pageCode,
            pagingCallback,
            type,
            keyword,
            tag,
            orderBy
        )
    ) {
        mimiDB.postDBItemDao()
            .pagingSourceByPageCode(pageCode)
    }.flow.map { pagingData ->
        pagingData.map {
            it.memberPostItem
        }
    }

    private fun clearResult(pageCode: String): Flow<Nothing?> {
        return flow {
            mimiDB.postDBItemDao().deleteItemByPageCode(pageCode)
            mimiDB.remoteKeyDao().deleteByPageCode(pageCode)
            emit(null)
        }
    }

}