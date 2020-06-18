package com.dabenxiang.mimi.view.postfavorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.vo.PlayListItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class PostFavoriteViewModel : BaseViewModel() {

    val dataCount = MutableLiveData<Int>()

    private val _favoriteList = MutableLiveData<PagedList<PlayListItem>>()
    val favoriteList: LiveData<PagedList<PlayListItem>> = _favoriteList

    fun initData() {
        viewModelScope.launch {
            val dataSrc = PostFavoriteListDataSource(viewModelScope, domainManager, postFavoritePagingCallback)
            dataSrc.isInvalid
            val factory = PostFavoriteListFactory(dataSrc, 1, false)
            val config = PagedList.Config.Builder()
                .setPageSize(PostFavoriteListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect { _favoriteList.postValue(it) }
        }
    }

    private val postFavoritePagingCallback = object : PostFavoritePagingCallback {
        override fun onLoading() { setShowProgress(true) }
        override fun onLoaded() { setShowProgress(false) }
        override fun onThrowable(throwable: Throwable) {}
        override fun onTotalCount(count: Int) {
            Timber.d("Count: $count")
            viewModelScope.launch { dataCount.value = count }
        }
    }
}