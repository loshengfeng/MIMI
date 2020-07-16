package com.dabenxiang.mimi.view.topup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.favroite.FavoritePlayListDataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TopUpViewModel : BaseViewModel() {

    private val _AgentList = MutableLiveData<PagedList<Any>>()
    val agentList: LiveData<PagedList<Any>> = _AgentList

    fun initData() {
        viewModelScope.launch {
            val dataSrc = TopUpProxyPayListDataSource(
                    viewModelScope,
                    domainManager,
                    topUpPagingCallback
            )
            dataSrc.isInvalid
            val factory = TopUpProxyPayListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                    .setPageSize(FavoritePlayListDataSource.PER_LIMIT.toInt())
                    .build()

            LivePagedListBuilder(factory, config).build().asFlow()
                    .collect { _AgentList.postValue(it) }
        }
    }

    fun getUserData(): ProfileItem {
        return accountManager.getProfile()
    }

    private val topUpPagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
    }
}