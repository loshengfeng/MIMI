package com.dabenxiang.mimi.view.topup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TopUpViewModel : BaseViewModel() {

    val TAG_MY_AVATAR = -1
    var currentItem: AgentItem? = null

    private val _agentList = MutableLiveData<PagedList<AgentItem>>()
    val agentList: LiveData<PagedList<AgentItem>> = _agentList

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    private val _avatar = MutableLiveData<ApiResult<AttachmentItem>>()
    val avatar: LiveData<ApiResult<AttachmentItem>> = _avatar

    private val _createChatRoomResult = MutableLiveData<ApiResult<String>>()
    val createChatRoomResult: LiveData<ApiResult<String>> = _createChatRoomResult

    private val _orderPackageResult =
        MutableLiveData<ApiResult<HashMap<PaymentType, ArrayList<OrderingPackageItem>>>>()
    val orderPackageResult: LiveData<ApiResult<HashMap<PaymentType, ArrayList<OrderingPackageItem>>>> =
        _orderPackageResult

    private val _isEmailConfirmed by lazy { MutableLiveData<ApiResult<Boolean>>() }
    val isEmailConfirmed: LiveData<ApiResult<Boolean>> get() = _isEmailConfirmed

    fun getProxyPayList() {
        viewModelScope.launch {
            val dataSrc = TopUpProxyPayListDataSource(
                viewModelScope,
                domainManager,
                topUpPagingCallback
            )
            dataSrc.isInvalid
            val factory = TopUpProxyPayListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(TopUpProxyPayListDataSource.PER_LIMIT)
                .build()

            LivePagedListBuilder(factory, config).build().asFlow()
                .collect { _agentList.postValue(it) }
        }
    }

    fun getOrderingPackage() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getOrderingPackage()
                if (!result.isSuccessful) throw HttpException(result)
                val orderPackageMap = hashMapOf<PaymentType, ArrayList<OrderingPackageItem>>()
                val orderingPackageItems = result.body()?.content ?: arrayListOf()
                orderingPackageItems.forEach {
                    if (orderPackageMap[it.paymentType] == null) {
                        orderPackageMap[it.paymentType] = arrayListOf()
                    }
                    orderPackageMap[it.paymentType]?.add(it)
                }
                emit(ApiResult.success(orderPackageMap))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _orderPackageResult.value = it }
        }
    }

    fun getMe() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                val meItem = result.body()?.content
                meItem?.let {
                    accountManager.setupProfile(it)
                    it.avatarAttachmentId?.also { id -> getAttachment(id.toString()) }
                }
                emit(ApiResult.success(meItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _meItem.value = it }
        }
    }

    fun getAttachment(id: String, position: Int = TAG_MY_AVATAR) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val item = AttachmentItem(
                    id = id,
                    fileArray = byteArray,
                    position = position
                )
                emit(ApiResult.success(item))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _avatar.value = it }
        }
    }

    fun createChatRoom(item: AgentItem) {
        currentItem = item
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = ChatRequest(userId = item.agentId?.toLong())
                val result = apiRepository.postChats(request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success((result.body() as ApiBaseItem<*>).content as String))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _createChatRoomResult.value = it }
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

    fun checkEmailConfirmed() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                val isEmailConfirmed = result.body()?.content?.isEmailConfirmed ?: false
                val status = result.isSuccessful && isEmailConfirmed
                emit(ApiResult.success(status))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _isEmailConfirmed.value = it }
        }
    }
}