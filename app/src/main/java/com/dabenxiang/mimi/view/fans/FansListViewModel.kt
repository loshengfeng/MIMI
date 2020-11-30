package com.dabenxiang.mimi.view.fans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FansListViewModel : BaseViewModel() {
//    private val _chatHistory = MutableLiveData<PagedList<ChatListItem>>()
//    val chatHistory: LiveData<PagedList<ChatListItem>> = _chatHistory
//
//    private val _pagingResult = MutableLiveData<ApiResult<Void>>()
//    val pagingResult: LiveData<ApiResult<Void>> = _pagingResult
//
//    private val pagingCallback = object : PagingCallback {
//        override fun onLoading() {
//
//        }
//
//        override fun onLoaded() {
//            _pagingResult.value = ApiResult.loaded()
//        }
//
//        override fun onThrowable(throwable: Throwable) {
//            _pagingResult.value = ApiResult.error(throwable)
//        }
//
//        override fun onSucceed() {
//            super.onSucceed()
//        }
//    }

//    private fun getChatHistoryPagingItems(): LiveData<PagedList<ChatListItem>> {
//        val dataSrc = ChatHistoryListDataSource(
//                viewModelScope,
//                domainManager,
//                pagingCallback
//        )
//        val factory = ChatHistoryListFactory(dataSrc)
//        val config = PagedList.Config.Builder()
//                .setPageSize(ChatHistoryListDataSource.PER_LIMIT.toInt())
//                .build()
//
//        return LivePagedListBuilder(factory, config).build()
//    }
//
    fun getFansList() {
        viewModelScope.launch {
//            getChatHistoryPagingItems().asFlow()
//                    .collect { _chatHistory.postValue(it) }
        }
    }
}