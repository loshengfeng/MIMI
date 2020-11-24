package com.dabenxiang.mimi.view.club

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.adapter.ClubTabTopicsListDataSource
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ClubTabViewModel : BaseViewModel() {
    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    fun getClubItemList(): Flow<PagingData<MemberClubItem>> {
        Timber.i("ClubTabFragment getClubItemList")
        return Pager(
            config = PagingConfig(pageSize = ClubTabTopicsListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                ClubTabTopicsListDataSource(
                    domainManager,
                    pagingCallback
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
        }

    }

}