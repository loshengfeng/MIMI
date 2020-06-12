package com.dabenxiang.mimi.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.RootCategoriesItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MainViewModel : BaseViewModel() {

    private val _adultMode = MutableLiveData(false)
    val adultMode: LiveData<Boolean> = _adultMode

    private val _categoriesData = MutableLiveData<RootCategoriesItem>()
    val categoriesData: LiveData<RootCategoriesItem> = _categoriesData

    fun setAdultMode(isAdult: Boolean) {
        if (_adultMode.value != isAdult) {
            _adultMode.value = isAdult
        }
    }

    fun loadHomeCategories() {
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
                .collect { resp ->
                    when (resp) {
                        is ApiResult.Success -> {
                            //Timber.d(resp.result.toString())
                            _categoriesData.value = resp.result.content
                        }
                        is ApiResult.Error -> Timber.e(resp.throwable)
                        is ApiResult.Loading -> Timber.d("Loading")
                        is ApiResult.Loaded -> Timber.d("Loaded")
                    }
                }
        }
    }
}