package com.dabenxiang.mimi.view.personal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PersonalViewModel : BaseViewModel() {

    var byteArray: ByteArray? = null

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    private val _apiSignOut = MutableLiveData<ApiResult<Nothing>>()
    val apiSignOut: LiveData<ApiResult<Nothing>> = _apiSignOut

    private val _unreadResult = MutableLiveData<ApiResult<Int>>()
    val unreadResult: LiveData<ApiResult<Int>> = _unreadResult

    fun getMe() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                val meItem = result.body()?.content
                meItem?.let {
                    accountManager.setupProfile(it)
                }
                emit(ApiResult.success(meItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _meItem.value = it }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut().collect {
                _apiSignOut.value = it
                _meItem.value = null
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

    private val _totalUnreadResult = MutableLiveData<ApiResult<Int>>()
    val totalUnreadResult: LiveData<ApiResult<Int>> = _totalUnreadResult

    fun getTotalUnread(){
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val chatUnreadResult = apiRepository.getUnread()
                val chatUnread = if (!chatUnreadResult.isSuccessful) 0 else chatUnreadResult.body()?.content?: 0
                val orderUnreadResult = apiRepository.getUnReadOrderCount()
                val orderUnread = if (!orderUnreadResult.isSuccessful) 0 else orderUnreadResult.body()?.content?: 0
                emit(ApiResult.success(chatUnread + orderUnread))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _totalUnreadResult.value = it }
        }
    }
}