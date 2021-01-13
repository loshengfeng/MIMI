package com.dabenxiang.mimi.view.orderinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.CreateOrderRequest
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OrderInfoViewModel : BaseViewModel() {

    private var _createOrderResult = MutableLiveData<ApiResult<Nothing>>()
    val createOrderResult: LiveData<ApiResult<Nothing>> = _createOrderResult

    fun createOrder(paymentType: PaymentType, packageId: Long) {
        viewModelScope.launch {
            flow {
                val request = CreateOrderRequest(paymentType.value, packageId)
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.createOrder(request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _createOrderResult.value = it }
        }
    }
}