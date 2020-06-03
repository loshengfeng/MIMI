package com.dabenxiang.mimi.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {

    private val _isAutoLogin = MutableLiveData<Boolean>()
    val isAutoLogin: LiveData<Boolean> = _isAutoLogin

    fun autoLogin() {
        if (accountManager.isAutoLogin()) {
            viewModelScope.launch {
                val profile = accountManager.getProfile()
                if (profile == null) {
                    _isAutoLogin.value = false
                } else
                    accountManager.signIn(profile.account, profile.password)
                        .collect {
                            when (it) {
                                is ApiResult.Empty -> _isAutoLogin.value = true
                                else -> _isAutoLogin.value = false
                            }
                        }
            }
        } else {
            _isAutoLogin.value = false
        }
    }
}