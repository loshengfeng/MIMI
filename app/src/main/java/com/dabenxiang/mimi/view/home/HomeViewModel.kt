package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CATEGORIES_LIMIT = "20"
        const val STATISTICS_LIMIT = 20
    }

    private val _tabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = _tabLayoutPosition

    fun setTopTabPosition(position: Int) {
        if (position != tabLayoutPosition.value) {
            _tabLayoutPosition.value = position
        }
    }

    fun loadNestedStatisticsListForCarousel(vh: HomeCarouselViewHolder, src: HomeTemplate.Carousel) {
        viewModelScope.launch {
            flow {
                val resp =
                    domainManager.getApiRepository().statisticsHomeVideos(isAdult = src.isAdult, offset = 0, limit = 5)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { resp ->
                    when (resp) {
                        is ApiResult.Success -> {
                            //Timber.d(resp.result.toString())
                            vh.submitList(resp.result.content?.statisticsItemToCarouselHolderItem(src.isAdult))
                        }
                        is ApiResult.Error -> Timber.e(resp.throwable)
                    }
                }
        }
    }

    fun loadNestedStatisticsList(vh: HomeStatisticsViewHolder, src: HomeTemplate.Statistics) {
        viewModelScope.launch {
            vh.activeTask {
                flow {
                    //val resp = domainManager.getApiRepository().searchHomeVideos(src.categories, null, null, null, src.isAdult, "0", CATEGORIES_LIMIT)
                    val resp = domainManager.getApiRepository()
                        .statisticsHomeVideos(category = src.categories, isAdult = src.isAdult, offset = 0, limit = STATISTICS_LIMIT)
                    if (!resp.isSuccessful) throw HttpException(resp)

                    emit(ApiResult.success(resp.body()))
                }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { resp ->
                        when (resp) {
                            is ApiResult.Success -> {
                                //Timber.d(resp.result.toString())
                                vh.submitList(resp.result.content?.statisticsItemToVideoItem(src.isAdult))
                            }
                            is ApiResult.Error -> Timber.e(resp.throwable)
                        }
                    }
            }
        }
    }

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    fun setupVideoList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = VideoListDataSource(isAdult, category ?: "", viewModelScope, domainManager.getApiRepository(), pagingCallback)
            dataSrc.isInvalid
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