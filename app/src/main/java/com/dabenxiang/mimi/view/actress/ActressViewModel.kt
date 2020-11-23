package com.dabenxiang.mimi.view.actress

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.myfollow.ClubFollowListDataSource
import kotlinx.coroutines.flow.Flow

class ActressViewModel : BaseViewModel() {

    private val clubPagingCallback = object : MyFollowPagingCallback {
        override fun onTotalCount(count: Long) {
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
        }
    }

    fun getClubList(): Flow<PagingData<ClubFollowItem>> {
        return Pager(
            config = PagingConfig(pageSize = ClubFollowListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                ClubFollowListDataSource(
                    domainManager,
                    clubPagingCallback
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

}