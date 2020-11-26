package com.dabenxiang.mimi.view.player.ui

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.GuessLikePagingCallBack
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.GuessLikeDataSource
import com.dabenxiang.mimi.view.player.GuessLikeFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PlayerDescriptionViewModel: BaseViewModel() {

    private val _getAdResult = MutableLiveData<ApiResult<AdItem>>()
    val getAdResult: LiveData<ApiResult<AdItem>> = _getAdResult

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private val _recyclerViewGuessLikeVisible = MutableLiveData<Int>()
    val recyclerViewGuessLikeVisible: LiveData<Int> = _recyclerViewGuessLikeVisible

    private val _isPageCallback = MutableLiveData<Boolean>()
    val isPageCallback: LiveData<Boolean> = _isPageCallback

    fun getAd(width: Int, height: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getAdRepository().getAD(width, height)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getAdResult.value = it }
        }
    }

    fun setupGuessLikeList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = GuessLikeDataSource(
                isAdult,
                category ?: "",
                viewModelScope,
                domainManager.getApiRepository(),
                pagingCallback
            )
            val factory = GuessLikeFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(GuessLikeDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : GuessLikePagingCallBack {
        override fun onLoadInit(initCount: Int) {
            _recyclerViewGuessLikeVisible.value =
                if (initCount == 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            _isPageCallback.value = true
        }

        override fun onLoading() {
            _isPageCallback.value = true
        }

        override fun onLoaded() {
            _isPageCallback.value = true
        }

        override fun onThrowable(throwable: Throwable) {
            _isPageCallback.value = true
        }
    }

}