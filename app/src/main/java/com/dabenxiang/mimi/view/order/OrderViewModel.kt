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
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListDataSource
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListFactory
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
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

    private val _createOrderChatResult = MutableLiveData<ApiResult<Triple<CreateOrderChatItem, ChatListItem, OrderItem>>>()
    val createOrderChatResult: LiveData<ApiResult<Triple<CreateOrderChatItem, ChatListItem, OrderItem>>> = _createOrderChatResult

    var unreadCount = 0
    var unreadOrderCount = 0

    fun getOrderByPaging2(type: OrderType?, update: ((PagedList<OrderItem>) -> Unit)) {
        viewModelScope.launch {
            val dataSrc =
                OrderListDataSource(viewModelScope, domainManager, pagingCallback, type)
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
        override fun onLoading() {
        }

        override fun onLoaded() {
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }

    private fun getChatHistoryPagingItems(): LiveData<PagedList<ChatListItem>> {
        val dataSrc = ChatHistoryListDataSource(
            viewModelScope,
            domainManager,
            chatPagingCallback
        )
        val factory = ChatHistoryListFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(ChatHistoryListDataSource.PER_LIMIT.toInt())
            .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getChatList(updateList: ((PagedList<ChatListItem>) -> Unit)) {
        viewModelScope.launch {
            getChatHistoryPagingItems().asFlow()
                .collect {
                    updateList(it)
                }
        }
    }

    fun getAttachment(id: String, position: Int, update: (Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val item = AttachmentItem(
                    id = id,
                    fileArray = byteArray,
                    position = position
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when(it) {
                        is ApiResult.Success -> {
                            val attachmentItem = it.result
                            LruCacheUtils.putLruArrayCache(
                                attachmentItem.id ?: "",
                                attachmentItem.fileArray ?: ByteArray(0)
                            )
                            update(attachmentItem.position ?: 0)
                        }
                    }
                }
        }
    }

    fun getProxyAttachment(id: String, update: (String) -> Unit) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                LruCacheUtils.putLruArrayCache(id, byteArray ?: ByteArray(0))
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Empty -> {
                            update(id)
                        }
                    }
                }
        }
    }

    fun getUnread(){
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

    fun createOrderChat(chatListItem: ChatListItem, orderItem: OrderItem) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().createOrderChat(CreateChatRequest(orderItem.id))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(Triple(result.body()?.content?: CreateOrderChatItem(), chatListItem, orderItem)))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _createOrderChatResult.value = it }
        }
    }
}