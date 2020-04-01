package com.dabenxiang.mimi.view.home

import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class HomeViewModel : BaseViewModel() {

    private val apiRepository: ApiRepository by inject()

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
                .collect { result ->
                    when (result) {
                        is ApiResult.Success -> Timber.d(result.result.toString())
                        is ApiResult.Error -> Timber.e(result.throwable)
                        is ApiResult.Loading -> Timber.d("Loading")
                        is ApiResult.Loaded -> Timber.d("Loaded")
                    }
                }
        }
    }
}