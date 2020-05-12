package com.dabenxiang.mimi.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class MainViewModel : BaseViewModel() {

    private val apiRepository: ApiRepository by inject()

    val enableNightMode = MutableLiveData<Boolean>()
    val adultMode = MutableLiveData<Boolean>(false)

    private val mCategoriesData = MutableLiveData<CategoriesItem>()
    val categoriesData: LiveData<CategoriesItem> = mCategoriesData

    fun loadHomeCategories() {
        viewModelScope.launch {
            flow {
                val resp = apiRepository.fetchHomeCategories()
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
                            mCategoriesData.value = resp.result.content
                        }
                        is ApiResult.Error -> Timber.e(resp.throwable)
                        is ApiResult.Loading -> Timber.d("Loading")
                        is ApiResult.Loaded -> Timber.d("Loaded")
                    }
                }
        }
    }
}