package com.dabenxiang.mimi.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.RootCategoriesItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel : BaseViewModel() {

    var needCloseApp = false // 判斷是否需要離開 app

    private val _adultMode = MutableLiveData(false)
    val adultMode: LiveData<Boolean> = _adultMode

    private val _categoriesData = MutableLiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>>()
    val categoriesData: LiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>> = _categoriesData

    private var _normal: CategoriesItem? = null
    val normal
        get() = _normal

    private var _adult: CategoriesItem? = null
    val adult
        get() = _adult

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

    /**
     * 按下 back 離開的 timer
     *
     */
    fun startBackExitAppTimer(){
        needCloseApp = true
        viewModelScope.launch {
            for (second in 2 downTo 0) {
                delay(1000)
            }
            needCloseApp = false
        }
    }
}