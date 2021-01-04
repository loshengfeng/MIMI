package com.dabenxiang.mimi.view.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.generalvideo.paging.VideoPagingSource
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoriesViewModel : BaseViewModel() {

    private val _getCategoryResult = MutableLiveData<ApiResult<ArrayList<String>>>()
    val getCategoryResult: LiveData<ApiResult<ArrayList<String>>> = _getCategoryResult

    fun getVideo(
        category: String?,
        orderByType: Int
    ): Flow<PagingData<StatisticsItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = ApiRepository.NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { VideoPagingSource(domainManager, category, orderByType, adWidth, adHeight, true,
                isCategoryPage = true
            ) }
        ).flow.cachedIn(viewModelScope)
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
}