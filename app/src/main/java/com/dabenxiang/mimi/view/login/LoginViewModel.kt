package com.dabenxiang.mimi.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isPasswordValid
import java.util.regex.Pattern

class LoginViewModel : BaseViewModel() {
    var type = TYPE_REGISTER

    private val _registerAccountError = MutableLiveData<Int>()
    val registerAccountError: LiveData<Int> = _registerAccountError

    private val _loginAccountError = MutableLiveData<Int>()
    val loginAccountError: LiveData<Int> = _loginAccountError

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int> = _emailError

    private val _loginPasswordError = MutableLiveData<Int>()
    val loginPasswordError: LiveData<Int> = _loginPasswordError

    private val _registerPasswordError = MutableLiveData<Int>()
    val registerPasswordError: LiveData<Int> = _registerPasswordError

    private val _confirmPasswordError = MutableLiveData<Int>()
    val confirmPasswordError: LiveData<Int> = _confirmPasswordError

    private val _registerResult = MutableLiveData<ApiResult<Nothing>>()
    val registerResult: LiveData<ApiResult<Nothing>> = _registerResult

    private val _loginResult = MutableLiveData<ApiResult<Nothing>>()
    val loginResult: LiveData<ApiResult<Nothing>> = _loginResult

    fun doRegisterValidateAndSubmit(account: String,
                                    email: String,
                                    pw: String,
                                    confirmPw: String
    ) {
        if (isValidateAccount(account) &&
            isValidateEmail(email) &&
            isValidateRegisterPassword(pw) &&
            isValidateConfirmPassword(pw, confirmPw)) {
            doRegister(account, confirmPw)
        }
    }

    fun doLoginValidateAndSubmit(account: String, password: String) {
        if (isValidateAccount(account) &&
            isValidateLoginPassword(password)) {
            doLogin(account, password)
        }
    }

    private fun isValidateAccount(account: String): Boolean {
        when (type) {
            TYPE_REGISTER -> {
                _registerAccountError.value = when {
                    account.isNullOrBlank() -> R.string.account_empty
                    !isAccountValid(account) -> R.string.account_format_error
                    else -> null
                }

                return when (_registerAccountError.value) {
                    null -> true
                    else -> false
                }
            }
            TYPE_LOGIN -> {
                _loginAccountError.value = when {
                    account.isNullOrBlank() -> R.string.account_empty
                    !isAccountValid(account) -> R.string.account_format_error
                    else -> null
                }

                return when (_loginAccountError.value) {
                    null -> true
                    else -> false
                }
            }
            else -> return true
        }
    }

    private fun isValidateEmail(email: String): Boolean {
        _emailError.value = when {
            email.isNullOrBlank() -> R.string.email_format_error_1
            !isEmailValid(email) -> R.string.email_format_error_2
            else -> null
        }

        return when (_emailError.value) {
            null -> true
            else -> false
        }
    }

    private fun isValidateRegisterPassword(pw: String): Boolean {
        _registerPasswordError.value = when {
            pw.isNullOrBlank() -> R.string.password_empty
            !isPasswordValid(pw) -> R.string.password_format_error_1
            else -> null
        }

        return when (_registerPasswordError.value) {
            null -> true
            else -> false
        }
    }

    private fun isValidateConfirmPassword(pw: String, confirmPw: String): Boolean {
        _confirmPasswordError.value = when {
            pw.isNullOrBlank() -> R.string.password_empty
            !isPasswordValid(pw) -> R.string.password_format_error_1
            pw != confirmPw -> R.string.password_format_error_2
            else -> null
        }

        return when (_confirmPasswordError.value) {
            null -> true
            else -> false
        }
    }

    private fun isValidateLoginPassword(pw: String): Boolean {
        _loginPasswordError.value = when {
            pw.isNullOrBlank() -> R.string.password_empty
            !isPasswordValid(pw) -> R.string.password_format_error_1
            else -> null
        }

        return when (_loginPasswordError.value) {
            null -> true
            else -> false
        }
    }

    private fun doLogin(account: String, password: String) {
        toastData.value = "doLogin"
//        viewModelScope.launch {
//            accountManager.login(account, password)
//                .collect { _loginResult.value = it }
//        }
    }

    private fun doRegister(account: String, password: String) {
        toastData.value = "doRegister"
//        viewModelScope.launch {
//            accountManager.register(account, password)
//                .collect { _loginResult.value = it }
//        }
    }
}