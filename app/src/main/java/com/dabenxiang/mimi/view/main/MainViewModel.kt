package com.dabenxiang.mimi.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel : BaseViewModel() {

    var needCloseApp = false // 判斷是否需要離開 app

    var isFromPlayer = false

    private val _adultMode = MutableLiveData(false)
    val adultMode: LiveData<Boolean> = _adultMode

    private val _categoriesData = MutableLiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>>()
    val categoriesData: LiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>> = _categoriesData

    private val _getAdResult = MutableLiveData<ApiResult<AdItem>>()
    val getAdResult: LiveData<ApiResult<AdItem>> = _getAdResult

    private val _getAdHomeResult = MutableLiveData<Pair<Int, ApiResult<AdItem>>>()
    val getAdHomeResult: LiveData<Pair<Int, ApiResult<AdItem>>> = _getAdHomeResult

    private var _normal: CategoriesItem? = null
    val normal
        get() = _normal

    private var _adult: CategoriesItem? = null
    val adult
        get() = _adult

    var isVersionChecked =false

    fun setupNormalCategoriesItem(item: CategoriesItem?) {
        _normal = item
    }

    fun setupAdultCategoriesItem(item: CategoriesItem?) {
        _adult = item
    }

    fun setAdultMode(isAdult: Boolean) {
        if (_adultMode.value != isAdult) {
            _adultMode.value = isAdult
        }
    }

    fun getHomeCategories() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().fetchHomeCategories()
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _categoriesData.value = it }
        }
    }

    fun getAd(position: Int, width: Int, height: Int) {
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
                .collect { _getAdHomeResult.value = Pair(position, it) }
        }
    }

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

    /**
     * 按下 back 離開的 timer
     *
     */
    fun startBackExitAppTimer() {
        needCloseApp = true
        viewModelScope.launch {
            for (second in 2 downTo 0) {
                delay(1000)
            }
            needCloseApp = false
        }
    }

    fun getCategory(title: String, isAdult: Boolean): CategoriesItem? {
        val item = if (isAdult) _adult else _normal
        var result: CategoriesItem? = null
            item?.categories?.forEach {
            if(it.name == title) {
                result = it
            }
        }
        return result
    }

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult
    fun sendPostReport(item: MemberPostItem, content: String) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val result = domainManager.getApiRepository().sendPostReport(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun sendCommentPostReport(
        postItem: MemberPostItem,
        postCommentItem: MembersPostCommentItem,
        content: String
    ) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.sendPostCommentReport(
                    postItem.id, postCommentItem.id!!, request
                )
                if (!result.isSuccessful) throw HttpException(result)
                postCommentItem.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

}