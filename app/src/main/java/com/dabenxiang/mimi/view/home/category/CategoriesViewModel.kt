package com.dabenxiang.mimi.view.home.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoriesViewModel : BaseViewModel() {

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private val _getCategoryResult = MutableLiveData<ApiResult<ArrayList<String>>>()
    val getCategoryResult: LiveData<ApiResult<ArrayList<String>>> = _getCategoryResult

    private val _onTotalCountResult = MutableLiveData<Long>()
    val onTotalCountResult: LiveData<Long> = _onTotalCountResult

    fun getVideoFilterList(
        category: String?,
        sorting: Int
    ) {
        viewModelScope.launch {
            val dataSrc =
                CategoriesDataSource(
                    orderByType = sorting,
                    category = category,
                    viewModelScope = viewModelScope,
                    domainManager = domainManager,
                    pagingCallback = pagingCallback,
                    adWidth = adWidth,
                    adHeight = adHeight
                )
            val factory = CategoriesFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    fun getCategory() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().fetchCategories()
                if (!resp.isSuccessful) throw HttpException(resp)
                val categories = arrayListOf<String>()
                resp.body()?.content?.get(0)?.categories?.forEach {
                    categories.add(it.name)
                }
                emit(ApiResult.success(categories))
            }
                .flowOn(Dispatchers.IO)
                .onStart { setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getCategoryResult.value = it }
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

        override fun onTotalCount(count: Long) {
            _onTotalCountResult.postValue(count)
        }
    }
}