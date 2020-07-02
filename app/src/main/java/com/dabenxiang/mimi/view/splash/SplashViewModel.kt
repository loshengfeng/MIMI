package com.dabenxiang.mimi.view.splash

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {

    private val _autoLoginResult = MutableLiveData<ApiResult<Nothing>>()
    val autoLoginResult: LiveData<ApiResult<Nothing>> = _autoLoginResult

    fun autoLogin() {
        if (accountManager.hasMemberToken()) {
            viewModelScope.launch {
                val profile = accountManager.getProfile()
                if (TextUtils.isEmpty(profile.account) || TextUtils.isEmpty(profile.password)) {
                    _autoLoginResult.value = ApiResult.success(null)
                } else {
                    signIn(profile.account, profile.password)
                }
            }
        } else {
            _autoLoginResult.value = ApiResult.success(null)
        }
    }

    private suspend fun signIn(account: String, password: String) {
        accountManager.signIn(account, password)
            .collect { _autoLoginResult.value = it }
    }
}