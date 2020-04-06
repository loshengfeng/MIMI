package com.dabenxiang.mimi.view.login

import com.dabenxiang.mimi.view.base.BaseViewModel

class LoginViewModel : BaseViewModel() {

//    fun autoLogin() {
//        when {
//            accountManager.isAutoLogin() -> {
//                viewModelScope.launch {
//                    val profile = accountManager.getProfile()
//                    accountManager.login(profile!!.account, profile.password)
//                        .collect { _loginResult.value = it }
//                }
//            }
//            else -> {
//                _isAutoLogin.value = false
//            }
//        }
//    }
}