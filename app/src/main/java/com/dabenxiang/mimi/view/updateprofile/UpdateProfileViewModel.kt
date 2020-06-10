package com.dabenxiang.mimi.view.updateprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel

class UpdateProfileViewModel : BaseViewModel() {
    lateinit var profileItem: ProfileItem

    private val _updateResult = MutableLiveData<ApiResult<Nothing>>()
    val updateResult: LiveData<ApiResult<Nothing>> = _updateResult

    fun updateProfile() {
        toastData.value =  "updateProfile"
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