package com.dabenxiang.mimi.view.club.latest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClubLatestViewModel : ClubViewModel() {

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _postItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val postItemListResult: LiveData<PagedList<MemberPostItem>> = _postItemListResult

    var totalCount: Int = 0

    fun getPostItemList() {
        viewModelScope.launch {
            getRecommendPostPagingItems()
                    .asFlow()
                    .collect { _postItemListResult.value = it }
        }
    }

    private fun getRecommendPostPagingItems(): LiveData<PagedList<MemberPostItem>> {
        val dataSourceFactory = object : DataSource.Factory<Int, MemberPostItem>() {
            override fun create(): DataSource<Int, MemberPostItem> {
                return ClubLatestListDataSource(
                        domainManager,
                        pagingCallback,
                        viewModelScope,
                        adWidth,
                        adHeight
                )
            }
        }

        val config = PagedList.Config.Builder()
                .setPrefetchDistance(4)
                .build()
        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
            _clubCount.postValue(0)
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            _clubCount.postValue(count.toInt())
        }
    }


}