package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CATEGORIES_LIMIT = 30
    }

    private val apiRepository: ApiRepository by inject()

    private val mTabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = mTabLayoutPosition

    fun setTopTabPosition(position: Int) {
        mTabLayoutPosition.value = position
    }

    fun loadNestedCategoriesList(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {
        viewModelScope.launch {
            adapter.activeTask {
                flow {
                    val resp = apiRepository.statisticsHomeVideos(StatisticsType.Newest, src.title ?: "", 0 , 30)
                    if (!resp.isSuccessful) throw HttpException(resp)

                    emit(ApiResult.success(resp.body()))
                }.flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { resp ->
                        when (resp) {
                            is ApiResult.Success -> {
                                //Timber.d(resp.result.toString())
                                adapter.notifyUpdated(resp.result.content)
                            }
                            is ApiResult.Error -> Timber.e(resp.throwable)
                            is ApiResult.Loading -> {

                            }
                            is ApiResult.Loaded -> {

                            }
                        }
                    }
            }
        }
    }
}