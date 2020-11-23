package com.dabenxiang.mimi.view.club.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import com.dabenxiang.mimi.view.home.postfollow.PostFollowFactory
import com.dabenxiang.mimi.view.myfollow.ClubFollowListDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubFollowViewModel : BaseViewModel() {

    private val _adResult = MutableLiveData<ApiResult<AdItem>>()
    val adResult: LiveData<ApiResult<AdItem>> = _adResult

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _clubIdList = ArrayList<Long>()

    fun getPostItemList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubPostFollowListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    ClubPostFollowListDataSource(
                            domainManager,
                            clubPagingCallback
                    )
                }
        )
                .flow
                .cachedIn(viewModelScope)
    }

    fun getAd() {
        viewModelScope.launch {
            flow {
                val adResult = domainManager.getAdRepository().getAD(adWidth, adHeight)
                if (!adResult.isSuccessful) throw HttpException(adResult)
                emit(ApiResult.success(adResult.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .collect { _adResult.value = it}
        }

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