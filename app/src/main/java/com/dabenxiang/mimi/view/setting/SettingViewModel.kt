package com.dabenxiang.mimi.view.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.EmailRequest
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class SettingViewModel : BaseViewModel() {
    private val _profileItem = MutableLiveData<ApiResult<ProfileItem>>()
    val profileItem: LiveData<ApiResult<ProfileItem>> = _profileItem

    private val _resendResult = MutableLiveData<ApiResult<Nothing>>()
    val resendResult: LiveData<ApiResult<Nothing>> = _resendResult

    private val _updateResult = MutableLiveData<ApiResult<Nothing>>()
    val updateResult: LiveData<ApiResult<Nothing>> = _updateResult

    var profileData: ProfileItem? = null

    fun getProfile() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getProfile()
                if (!result.isSuccessful) throw HttpException(result)
                profileData = result.body()?.content
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _profileItem.value = it }
        }
    }

    fun resendEmail() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().resendEmail(EmailRequest(profileData?.email))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _resendResult.value = it }
        }
    }

    fun updateProfile() {
//        viewModelScope.launch {
//            flow {
//                val result = profileData?.let { domainManager.getApiRepository().updateProfile(it) }
//                if (!result.isSuccessful) throw HttpException(result)
//                emit(ApiResult.success(null))
//            }
//                .onStart { emit(ApiResult.loading()) }
//                .catch { e -> emit(ApiResult.error(e)) }
//                .onCompletion { emit(ApiResult.loaded()) }
//                .collect { _updateResult.value = it }
//        }
    }
}