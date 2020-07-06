package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBasePagingItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CAROUSEL_LIMIT = 5
        const val STATISTICS_LIMIT = 20
    }

    private var _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private var _tabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = _tabLayoutPosition

    private var _carouselResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val carouselResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _carouselResult

    private var _videosResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val videosResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _videosResult

    fun setTopTabPosition(position: Int) {
        if (position != tabLayoutPosition.value) {
            _tabLayoutPosition.value = position
        }
    }

    fun loadNestedStatisticsListForCarousel(position: Int, src: HomeTemplate.Carousel) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().statisticsHomeVideos(
                    isAdult = src.isAdult,
                    offset = 0,
                    limit = CAROUSEL_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _carouselResult.value = Pair(position, it) }
        }
    }

    fun loadNestedStatisticsList(position: Int, src: HomeTemplate.Statistics) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().statisticsHomeVideos(
                    category = src.categories,
                    isAdult = src.isAdult,
                    offset = 0,
                    limit = STATISTICS_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videosResult.value = Pair(position, it) }
        }
    }

    fun setupVideoList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = VideoListDataSource(
                isAdult,
                category,
                viewModelScope,
                domainManager.getApiRepository(),
                pagingCallback
            )
            val factory = VideoListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
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

        override fun onThrowable(throwable: Throwable) {
        }
    }
}