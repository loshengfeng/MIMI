package com.dabenxiang.mimi.view.home.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import com.dabenxiang.mimi.view.home.video.VideoFactory
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
            val dataSrc =
                VideoDataSource(
                    isAdult,
                    category ?: "",
                    viewModelScope,
                    domainManager,
                    pagingCallback
                )
            val factory =
                VideoFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(VideoDataSource.PER_LIMIT.toInt())
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