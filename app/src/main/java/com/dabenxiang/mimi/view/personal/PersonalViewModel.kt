package com.dabenxiang.mimi.view.personal


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PersonalViewModel : BaseViewModel() {

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem : LiveData<ApiResult<MeItem>> = _meItem

    private val _apiSignOut = MutableLiveData<ApiResult<Nothing>>()
    val apiSignOut: LiveData<ApiResult<Nothing>> = _apiSignOut

    @ExperimentalCoroutinesApi
    fun getMe() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _meItem.value = it
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut().collect {
                _apiSignOut.value = it
            }
        }
    }
}