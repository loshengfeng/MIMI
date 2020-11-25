package com.dabenxiang.mimi.view.club.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubFollowViewModel : BaseViewModel() {

    private val _adResult = MutableLiveData<ApiResult<AdItem>>()
    val adResult: LiveData<ApiResult<AdItem>> = _adResult

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount


    fun getPostItemList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubPostFollowListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    ClubPostFollowListDataSource(
                            domainManager,
                            pagingCallback
                    )
                }
        )
                .flow
                .onStart {  setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
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

    private val pagingCallback = object : PagingCallback {

        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }

    }

}