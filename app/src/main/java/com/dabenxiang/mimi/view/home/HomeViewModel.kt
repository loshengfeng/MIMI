package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.model.holder.BaseVideoItem
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

    private val _tabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = _tabLayoutPosition

    fun setTopTabPosition(position: Int) {
        if (position != tabLayoutPosition.value) {
            _tabLayoutPosition.value = position
        }
    }

    fun loadNestedCategoriesList(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {
        viewModelScope.launch {
            adapter.activeTask {
                flow {
                    val resp = apiRepository.statisticsHomeVideos(StatisticsType.Newest, src.title ?: "", 0, 30)
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
                                adapter.notifyUpdated(resp.result.content)
                            }
                            is ApiResult.Error -> Timber.e(resp.throwable)
                            //is ApiResult.Loading -> setShowProgress(true)
                            //is ApiResult.Loaded -> setShowProgress(false)
                        }
                    }
            }
        }
    }

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    fun setupVideoList(isAdult: Boolean, category: String?) {
        viewModelScope.launch {
            val dataSrc = VideoListDataSource(isAdult, category ?: "", viewModelScope, apiRepository, pagingCallback)
            val factory = VideoListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    val pagingCallback = object : PagingCallback {
        override fun onLoading() {
        }

        override fun onLoaded() {
        }

        override fun onThrowable(throwable: Throwable) {
        }

    }
}