package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoriesViewModel : BaseViewModel() {

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private val filterPositionDataList by lazy {
        val map = mutableMapOf<Int, MutableLiveData<Int>>()
        repeat(3) {
            map[it] = MutableLiveData(0)
        }
        return@lazy map
    }

    fun filterPositionData(index: Int): LiveData<Int>? = filterPositionDataList[index]

    fun updatedFilterPosition(index: Int, position: Int) {
        filterPositionDataList[index]?.value = position
    }

    fun setupVideoList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = VideoListDataSource(isAdult, category ?: "", viewModelScope, domainManager.getApiRepository(), pagingCallback)
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
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }
}