package com.dabenxiang.mimi.view.club.latest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*

class ClubLatestModel : BaseViewModel() {

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _clubIdList = ArrayList<Long>()

    fun getPostItemList(category: Int): Flow<PagingData<StatisticsItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubLatestListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    ClubLatestListDataSource(
                            domainManager,
                            clubPagingCallback,
                            adWidth,
                            adHeight,
                            category,
                            false

                    )
                }
        )
                .flow
                .cachedIn(viewModelScope)
    }

    private val clubPagingCallback = object : MyFollowPagingCallback {
        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _clubIdList.clear()
            _clubIdList.addAll(list)
        }
    }

}