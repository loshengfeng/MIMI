package com.dabenxiang.mimi.view.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.PagingCallback
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderViewModel : BaseViewModel() {

    private val _orderList = MutableLiveData<PagedList<OrderItem>>()
    val orderList: LiveData<PagedList<OrderItem>> = _orderList

    fun getOrder(type: Int) {
        viewModelScope.launch {
            val dataSrc = OrderListDataSource(viewModelScope, domainManager, pagingCallback)
            dataSrc.isInvalid
            val factory = OrderListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(OrderListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _orderList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() { setShowProgress(true) }
        override fun onLoaded() { setShowProgress(false) }
        override fun onThrowable(throwable: Throwable) {}
    }
}