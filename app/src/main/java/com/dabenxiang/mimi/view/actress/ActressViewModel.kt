package com.dabenxiang.mimi.view.actress

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.inviteviprecord.InviteVipRecordAdapter
import com.dabenxiang.mimi.view.inviteviprecord.InviteVipRecordListDataSource
import com.dabenxiang.mimi.view.inviteviprecord.InviteVipRecordListFactory
import com.dabenxiang.mimi.view.myfollow.ClubFollowListDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActressViewModel : BaseViewModel() {

    private val actressPagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
        }
    }

    private fun getActressPagingItems(): LiveData<PagedList<ReferrerHistoryItem>> {
        val dataSrc = ActressListDataSource(
            viewModelScope,
            domainManager,
            actressPagingCallback
        )
        val factory = ActressListFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(InviteVipRecordListDataSource.PER_LIMIT.toInt())
            .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getActressList(adapter: ActressAdapter) {
        viewModelScope.launch {
            getActressPagingItems().asFlow()
                .collect {
                    adapter.submitList(it)
                }
        }
    }

}