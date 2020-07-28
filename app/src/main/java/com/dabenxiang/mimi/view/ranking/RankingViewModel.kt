package com.dabenxiang.mimi.view.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.api.vo.RankingItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.order.OrderListDataSource
import com.dabenxiang.mimi.view.order.OrderListFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RankingViewModel : BaseViewModel() {

    private val _rankingList = MutableLiveData<PagedList<RankingItem>>()
    val rankingList: LiveData<PagedList<RankingItem>> = _rankingList

    fun getRanking(statisticsType: StatisticsType = StatisticsType.TODAY, postType: PostType= PostType.VIDEO) {
        viewModelScope.launch {
            val dataSrc = RankingDataSource(viewModelScope, domainManager, pagingCallback, statisticsType, postType)
            dataSrc.isInvalid
            val factory = RankingFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(RankingDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _rankingList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
    }
}