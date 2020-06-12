package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.parser
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CATEGORIES_LIMIT = "20"
    }

    private val _tabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = _tabLayoutPosition

    private var _selectedCategoryTitle = ""
    val selectedCategoryTitle = _selectedCategoryTitle

    fun setTopTabPosition(position: Int) {
        if (position != tabLayoutPosition.value) {
            _tabLayoutPosition.value = position
        }
    }

    fun loadNestedCategoriesList(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {
        viewModelScope.launch {
            adapter.activeTask {
                flow {
                    val resp = domainManager.getApiRepository().searchHomeVideos(src.categories, null, null, null, src.isAdult, "0", CATEGORIES_LIMIT)
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
                                adapter.submitList(resp.result.content?.videos?.parser(src.isAdult))
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

    fun setupVideoList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = VideoListDataSource(isAdult, category ?: "", viewModelScope, domainManager.getApiRepository(), pagingCallback)
            dataSrc.isInvalid
            val factory = VideoListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : PagingCallback {
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