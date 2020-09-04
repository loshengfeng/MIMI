package com.dabenxiang.mimi.view.dialog.chooseclub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChooseClubDialogViewModel : BaseViewModel() {

    private val _postList = MutableLiveData<PagedList<Any>>()
    val postList: LiveData<PagedList<Any>> = _postList

    private val _loadingStatus = MutableLiveData<Boolean>()
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private val _totalCount = MutableLiveData<Long>()
    val totalCount: MutableLiveData<Long> = _totalCount

    fun getClubList() {
        viewModelScope.launch {
            val dataSrc = ChooseClubDataSource(
                viewModelScope,
                domainManager,
                pagingCallback
            )
            dataSrc.isInvalid

            val factory = ChooseClubFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(ChooseClubDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow()
                .collect { _postList.postValue(it) }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            _loadingStatus.value = true
        }

        override fun onLoaded() {
            _loadingStatus.value = false
        }

        override fun onSucceed() {

        }

        override fun onThrowable(throwable: Throwable) {
        }

        override fun onTotalCount(count: Long) {
            _totalCount.postValue(count)
        }
    }
}