package com.dabenxiang.mimi.view.inviteviprecord

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListDataSource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class InviteVipRecordViewModel : BaseViewModel() {

    private val chatPagingCallback = object : PagingCallback {
        override fun onLoading() {}
        override fun onLoaded() {}
        override fun onThrowable(throwable: Throwable) {}
    }

    private fun getChatHistoryPagingItems(updateNoData: ((Int) -> Unit) = {}): LiveData<PagedList<ReferrerHistoryItem>> {
        val dataSrc = InviteVipRecordListDataSource(
            viewModelScope,
            domainManager,
            chatPagingCallback,
            updateNoData
        )
        val factory = InviteVipRecordListFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(ChatHistoryListDataSource.PER_LIMIT.toInt())
            .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getChatList(adapter: InviteVipRecordAdapter) {
        viewModelScope.launch {
            getChatHistoryPagingItems().asFlow()
                .collect {
                    Timber.d("catkingg: $it")
                    adapter.submitList(it)
                }
        }
    }
}