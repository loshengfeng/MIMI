package com.dabenxiang.mimi.view.home.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.VideoSearchItem
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import com.dabenxiang.mimi.view.home.video.VideoFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoriesViewModel : BaseViewModel() {

    var adWidth = 0
    var adHeight = 0

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private val _filterList = MutableLiveData<PagedList<BaseVideoItem>>()
    val filterList: LiveData<PagedList<BaseVideoItem>> = _filterList

    private val _getCategoryDetailResult = MutableLiveData<ApiResult<VideoSearchItem>>()
    val getCategoryDetailResult: LiveData<ApiResult<VideoSearchItem>> = _getCategoryDetailResult

    private val filterPositionDataList by lazy {
        val map = mutableMapOf<Int, MutableLiveData<Int>>()
        repeat(3) {
            map[it] = MutableLiveData(0)
        }
        return@lazy map
    }

    fun filterPositionData(index: Int): LiveData<Int>? = filterPositionDataList[index]

    fun updatedFilterPosition(index: Int, position: Int) {
        filterPositionDataList[index]?.value = position
    }

    fun setupVideoList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc =
                VideoDataSource(
                    isAdult,
                    category,
                    viewModelScope,
                    domainManager,
                    pagingCallback,
                    adWidth,
                    adHeight
                )
            val factory =
                VideoFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    fun getVideoFilterList(category: String?, country: String?, years: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc =
                CategoriesDataSource(
                    isAdult = isAdult,
                    category = category,
                    country = country,
                    years = years,
                    viewModelScope = viewModelScope,
                    domainManager = domainManager,
                    pagingCallback = pagingCallback,
                    adWidth = adWidth,
                    adHeight = adHeight
                )
            val factory =
                CategoriesFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _filterList.postValue(it)
            }
        }
    }

    fun getCategoryDetail(category: String, isAdult: Boolean) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().searchHomeVideos(
                    category = category,
                    isAdult = isAdult,
                    offset = "0",
                    limit = "1"
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getCategoryDetailResult.value = it }
        }
    }

    val pagingCallback = object : PagingCallback {
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