package com.dabenxiang.mimi.view.my_pages.pages.fans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.FansItem
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class FansListViewModel : BaseViewModel() {

    private val _fansCount = MutableLiveData<Long>()
    val fansCount: LiveData<Long> = _fansCount

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    fun getData(adapter: FansListAdapter) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getFansList().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun getFansList(): Flow<PagingData<FansItem>> {
        return Pager(
            config = PagingConfig(pageSize = FansDataSource.PER_LIMIT),
            pagingSourceFactory = {
                FansDataSource(
                    domainManager,
                    pagingCallback
                )
            }
        )
            .flow
//            .onStart {  setShowProgress(true) }
//            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            super.onLoading()
        }

        override fun onLoaded() {
            super.onLoaded()
        }

        override fun onSucceed() {
            super.onSucceed()
        }

        override fun onTotalCount(count: Long) {
            _fansCount.postValue(count)

        }
    }

    fun followPost(item: FansItem, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.userId)
                    else -> apiRepository.cancelFollowPost(item.userId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followPostResult.value = it }
        }
    }

}