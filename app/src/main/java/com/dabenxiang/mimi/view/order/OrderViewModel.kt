package com.dabenxiang.mimi.view.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class OrderViewModel : BaseViewModel() {

    private val _orderList = MutableLiveData<PagedList<OrderItem>>()
    val orderList: LiveData<PagedList<OrderItem>> = _orderList

    fun getOrder2(update:((PagedList<OrderItem>) -> Unit)) {
        viewModelScope.launch {
            val dataSrc = OrderListDataSource(viewModelScope, domainManager, pagingCallback)
            dataSrc.isInvalid
            val factory = OrderListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(OrderListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                update(it)
            }
        }
    }

    fun getOrder(): Flow<PagingData<OrderItem>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { OrderPagingSource(domainManager) }
        ).flow.cachedIn(viewModelScope)
    }

    fun getOrderLiveData(): LiveData<PagingData<OrderItem>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { OrderPagingSource(domainManager) }
        ).liveData
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