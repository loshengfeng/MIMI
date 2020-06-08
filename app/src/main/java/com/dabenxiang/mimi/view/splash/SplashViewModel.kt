package com.dabenxiang.mimi.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {

    private val _autoLoginResult = MutableLiveData<Boolean>()
    val autoLoginResult: LiveData<Boolean> = _autoLoginResult

    fun autoLogin() {
        if (accountManager.hasMemberToken()) {
            viewModelScope.launch {
                val profile = accountManager.getProfile()
                if (profile.account.isEmpty() || profile.password.isEmpty()) {
                    _autoLoginResult.value = false
                } else
                    accountManager.signIn(profile.account, profile.password)
                        .collect {
                            when (it) {
                                is ApiResult.Empty -> _autoLoginResult.value = true
                                else -> _autoLoginResult.value = false
                            }
                        }
            }
        } else {
            _autoLoginResult.value = false
        }
    }
}