package com.dabenxiang.mimi.view.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListDataSource
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OrderViewModel : BaseViewModel() {

    private val _balanceResult = MutableLiveData<BalanceItem>()
    val balanceResult: LiveData<BalanceItem> = _balanceResult

    private val _unreadResult = MutableLiveData<ApiResult<Int>>()
    val unreadResult: LiveData<ApiResult<Int>> = _unreadResult

    private val _unreadOrderResult = MutableLiveData<ApiResult<Int>>()
    val unreadOrderResult: LiveData<ApiResult<Int>> = _unreadOrderResult

    private val _createOrderChatResult =
        MutableLiveData<ApiResult<Triple<CreateOrderChatItem, ChatListItem, OrderItem>>>()
    val createOrderChatResult: LiveData<ApiResult<Triple<CreateOrderChatItem, ChatListItem, OrderItem>>> =
        _createOrderChatResult

    var unreadCount = 0
    var unreadOrderCount = 0

    fun getBalanceItem() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getOrder("0", "1")
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content?.balance))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> _balanceResult.postValue(it.result)
                    }
                }
        }
    }

    fun getOrderByPaging2(
        type: OrderType?,
        update: ((PagedList<OrderItem>) -> Unit),
        updateNoData: ((Int) -> Unit)
    ) {
        viewModelScope.launch {
            val dataSrc = OrderListDataSource(
                viewModelScope, domainManager, pagingCallback, type, updateNoData
            )
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

    fun getOrderByPaging3(): Flow<PagingData<OrderItem>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { OrderPagingSource(domainManager) }
        ).flow.cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}

        override fun onGetAny(obj: Any?) {
            if (obj is BalanceItem) {
                _balanceResult.postValue(obj)
            }
        }
    }

    private val chatPagingCallback = object : PagingCallback {
        override fun onLoading() {}
        override fun onLoaded() {}
        override fun onThrowable(throwable: Throwable) {}
    }

    private fun getChatHistoryPagingItems(updateNoData: ((Int) -> Unit) = {}): LiveData<PagedList<ChatListItem>> {
        val dataSrc = ChatHistoryListDataSource(
            viewModelScope,
            domainManager,
            chatPagingCallback,
            updateNoData
        )
        val factory = ChatHistoryListFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(ChatHistoryListDataSource.PER_LIMIT.toInt())
            .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getChatList(
        updateList: ((PagedList<ChatListItem>) -> Unit),
        updateNoData: ((Int) -> Unit)
    ) {
        viewModelScope.launch {
            getChatHistoryPagingItems().asFlow()
                .collect {
                    updateList(it)
                }
        }
    }

    fun getUnread() {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getUnread()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content as Int))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _unreadResult.value = it }
        }
    }

    fun getUnReadOrderCount() {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getUnReadOrderCount()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content as Int))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _unreadOrderResult.value = it }
        }
    }

    fun createOrderChat(
        chatListItem: ChatListItem,
        orderItem: OrderItem,
        updateChatId: ((CreateOrderChatItem) -> Unit)
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .createOrderChat(CreateChatRequest(orderItem.id))
                if (!result.isSuccessful) throw HttpException(result)
                val createOrderChatItem = result.body()?.content ?: CreateOrderChatItem()
                updateChatId(createOrderChatItem)
                emit(ApiResult.success(Triple(createOrderChatItem, chatListItem, orderItem)))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _createOrderChatResult.value = it }
        }
    }

    fun getProxyOrderUnread(update: ((Int, Boolean) -> Unit)) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getOrderByType(OrderType.MERCHANT2USER, "0", "1")
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()?.content?.orders?.get(0)
                item?.also {
                    emit(ApiResult.success(it.lastReplyTime?.time ?: 0 > it.lastReadTime?.time ?: 0))
                } ?: emit(ApiResult.success(true))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> update(0, it.result)
                    }
                }
        }
    }

    fun getChatUnread(update: ((Int, Boolean) -> Unit)) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getUnread()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success((result.body()?.content as Int) > 0))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> update(1, it.result)
                    }
                }
        }
    }
}