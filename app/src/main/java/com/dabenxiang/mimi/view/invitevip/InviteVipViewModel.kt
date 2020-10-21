package com.dabenxiang.mimi.view.invitevip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PromotionItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class InviteVipViewModel : BaseViewModel() {
    private val _promotionItem = MutableLiveData<ApiResult<PromotionItem>>()
    val promotionItem: LiveData<ApiResult<PromotionItem>> = _promotionItem

    private val _showCopyHint = MutableLiveData<Boolean>().apply { postValue(false)}
    val showCopyHint: LiveData<Boolean> = _showCopyHint

    var promotionData: PromotionItem? = null

    fun getPromotionItem() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getPromotionItem(domainManager.getWebDomain())
                if (!result.isSuccessful) throw HttpException(result)
                promotionData = result.body()?.content
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _promotionItem.value = it }
        }
    }

    fun setShowCopyHint(duration: Long){
        viewModelScope.launch {
            _showCopyHint.postValue(true)
            delay(duration)
            _showCopyHint.setNot()
        }
    }


}