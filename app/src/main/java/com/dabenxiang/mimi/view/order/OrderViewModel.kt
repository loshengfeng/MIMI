package com.dabenxiang.mimi.view.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListDataSource
import com.dabenxiang.mimi.view.chathistory.ChatHistoryListFactory
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class OrderViewModel : BaseViewModel() {

    private val _orderList = MutableLiveData<PagedList<OrderItem>>()
    val orderList: LiveData<PagedList<OrderItem>> = _orderList

    fun getOrderByPaging2(isOnline: Boolean?, update: ((PagedList<OrderItem>) -> Unit)) {
        viewModelScope.launch {
            val dataSrc =
                OrderListDataSource(viewModelScope, domainManager, pagingCallback, isOnline)
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

    private val _chatHistory = MutableLiveData<PagedList<ChatListItem>>()
    val chatHistory: LiveData<PagedList<ChatListItem>> = _chatHistory

    private val _pagingResult = MutableLiveData<ApiResult<Void>>()
    val pagingResult: LiveData<ApiResult<Void>> = _pagingResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private val chatPagingCallback = object : PagingCallback {
        override fun onLoading() {

        }

        override fun onLoaded() {
            _pagingResult.value = ApiResult.loaded()
        }

        override fun onThrowable(throwable: Throwable) {
            _pagingResult.value = ApiResult.error(throwable)
        }

        override fun onSucceed() {
            super.onSucceed()
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
}