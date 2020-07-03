package com.dabenxiang.mimi.view.myfollow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_CLUB
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_MEMBER
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyFollowViewModel : BaseViewModel() {
    private val _clubList = MutableLiveData<PagedList<ClubFollowItem>>()
    val clubList: LiveData<PagedList<ClubFollowItem>> = _clubList

    private val _memberList = MutableLiveData<PagedList<MemberFollowItem>>()
    val memberList: LiveData<PagedList<MemberFollowItem>> = _memberList

    fun initData(type: Int) {
        when (type) {
            TYPE_MEMBER -> {
                viewModelScope.launch {
                    val dataSrc =
                        MemberFollowListDataSource(viewModelScope, domainManager, pagingCallback)
                    dataSrc.isInvalid
                    val factory = MemberFollowListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(ClubFollowListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect {
                        _memberList.postValue(it)
                    }
                }
            }

            TYPE_CLUB -> {
                viewModelScope.launch {
                    val dataSrc =
                        ClubFollowListDataSource(viewModelScope, domainManager, pagingCallback)
                    dataSrc.isInvalid
                    val factory = ClubFollowListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(ClubFollowListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect {
                        _clubList.postValue(it)
                    }
                }
            }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
    }
}
