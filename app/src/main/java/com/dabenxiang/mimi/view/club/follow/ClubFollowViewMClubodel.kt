package com.dabenxiang.mimi.view.club.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import com.dabenxiang.mimi.view.home.postfollow.PostFollowFactory
import com.dabenxiang.mimi.view.myfollow.ClubFollowListDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubFollowViewMClubodel : BaseViewModel() {

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _clubIdList = ArrayList<Long>()

    fun getPostItemList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubPostFollowListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    ClubPostFollowListDataSource(
                            domainManager,
                            clubPagingCallback,
                            adWidth,
                            adHeight
                    )
                }
        )
                .flow
                .cachedIn(viewModelScope)
    }

    private val clubPagingCallback = object : MyFollowPagingCallback {
        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _clubIdList.clear()
            _clubIdList.addAll(list)
        }
    }

}