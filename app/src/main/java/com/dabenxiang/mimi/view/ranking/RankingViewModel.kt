package com.dabenxiang.mimi.view.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RankingViewModel : BaseViewModel() {

    private val _rankingList = MutableLiveData<PagedList<PostStatisticsItem>>()
    val rankingList: LiveData<PagedList<PostStatisticsItem>> = _rankingList

    private val _rankingVideosList = MutableLiveData<PagedList<StatisticsItem>>()
    val rankingVideosList: LiveData<PagedList<StatisticsItem>> = _rankingVideosList

    private val _isLoadingData = MutableLiveData<Boolean>()
    val isLoadingData: LiveData<Boolean> = _isLoadingData

    var statisticsTypeSelected: StatisticsType = StatisticsType.TODAY
    var postTypeSelected: PostType = PostType.VIDEO_ON_DEMAND


    fun setPostType(position: Int) {
        postTypeSelected = when (position) {
            0->PostType.VIDEO_ON_DEMAND
            1 -> PostType.VIDEO
            else -> PostType.IMAGE
        }
    }

    fun setStatisticsTypeFunction(position: Int) {
        statisticsTypeSelected = when (position) {
            0 -> StatisticsType.TODAY
            1 -> StatisticsType.WEEK
            else -> StatisticsType.MONTH
        }
    }

    fun getRankingList() {
        viewModelScope.launch {
            if (postTypeSelected == PostType.VIDEO_ON_DEMAND) {
                getVideosRanking()
            } else {
                getRanking()
            }
        }
    }

    private suspend fun getRanking() {
        val dataSrc = RankingDataSource(
            viewModelScope,
            domainManager,
            pagingCallback,
            statisticsTypeSelected,
            postTypeSelected
        )
        dataSrc.isInvalid
        val factory = RankingFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(RankingDataSource.PER_LIMIT.toInt())
            .build()

        LivePagedListBuilder(factory, config).build().asFlow().collect {
            _rankingList.postValue(it)
        }
    }

    private suspend fun getVideosRanking() {
        val dataSrc = RankingVideosDataSource(
            viewModelScope,
            domainManager,
            pagingCallback,
            statisticsTypeSelected
        )
        dataSrc.isInvalid
        val factory = RankingVideosFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(RankingDataSource.PER_LIMIT.toInt())
            .build()

        LivePagedListBuilder(factory, config).build().asFlow().collect {
            _rankingVideosList.postValue(it)
        }

    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
            _isLoadingData.value = true
        }

        override fun onLoaded() {
            setShowProgress(false)
            _isLoadingData.value = false
        }

        override fun onThrowable(throwable: Throwable) {}
    }
}