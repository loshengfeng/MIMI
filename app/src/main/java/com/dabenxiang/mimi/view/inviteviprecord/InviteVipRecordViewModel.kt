package com.dabenxiang.mimi.view.inviteviprecord

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InviteVipRecordViewModel : BaseViewModel() {

    private val _onTotalCountResult = MutableLiveData<Long>()
    val onTotalCountResult: LiveData<Long> = _onTotalCountResult

    private val inviteVipRecordPagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }
        override fun onLoaded() {
            setShowProgress(false)
        }
        override fun onThrowable(throwable: Throwable) {}
        override fun onTotalCount(count: Long) {
            _onTotalCountResult.postValue(count)
        }
    }

    private fun getInviteVipRecordPagingItems(): LiveData<PagedList<ReferrerHistoryItem>> {
        val dataSrc = InviteVipRecordListDataSource(
            viewModelScope,
            domainManager,
            inviteVipRecordPagingCallback
        )
        val factory = InviteVipRecordListFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(InviteVipRecordListDataSource.PER_LIMIT.toInt())
            .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getInviteVipRecordList(adapter: InviteVipRecordAdapter) {
        viewModelScope.launch {
            getInviteVipRecordPagingItems().asFlow()
                .collect {
                    adapter.submitList(it)
                }
        }
    }
}