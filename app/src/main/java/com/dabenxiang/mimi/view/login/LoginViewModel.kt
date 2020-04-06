package com.dabenxiang.mimi.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseViewModel
import java.util.regex.Pattern

class LoginViewModel : BaseViewModel() {

    companion object {
        const val REGEXP_USER_NAME = "^[a-zA-Z0-9]{5,20}$"
        const val REGEXP_PASSWORD = "^\\S{8,20}\$"
        const val REGEXP_EMAIL = "^[A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$"
    }

    private val _accountError = MutableLiveData<Int>()
    val accountError: LiveData<Int> = _accountError

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int> = _emailError

    private val _loginPasswordError = MutableLiveData<Int>()
    val loginPasswordError: LiveData<Int> = _loginPasswordError

    private val _registerPasswordError = MutableLiveData<Int>()
    val registerPasswordError: LiveData<Int> = _registerPasswordError

    private val _confirmPasswordError = MutableLiveData<Int>()
    val confirmPasswordError: LiveData<Int> = _confirmPasswordError

//    private val _loginResult = MutableLiveData<ApiResult<Nothing>>()
//    val loginResult: LiveData<ApiResult<Nothing>> = _loginResult

    fun doRegisterValidateAndSubmit(account: String,
                                    email: String,
                                    pw: String,
                                    confirmPw: String
    ) {
        if (isValidateAccount(account) &&
            isValidateEmail(email) &&
            isValidateRegisterPassword(pw) &&
            isValidateConfirmPassword(pw, confirmPw)) {
            doLogin(account, confirmPw)
        }
    }

    fun doLoginValidateAndSubmit(account: String, password: String) {
        if (isValidateAccount(account) &&
            isValidateLoginPassword(password)) {
            doLogin(account, password)
        }
    }

    private fun isValidateAccount(userName: String): Boolean {
        _accountError.value = when {
            userName.isNullOrBlank() -> R.string.account_empty
            !Pattern.matches(REGEXP_USER_NAME, userName) -> R.string.account_format_error
            else -> null
        }

        return when (_accountError.value) {
            null -> true
            else -> false
        }
    }

    private fun isValidateEmail(email: String): Boolean {
        _accountError.value = when {
            email.isNullOrBlank() -> R.string.email_format_error
            !Pattern.matches(REGEXP_EMAIL, email) -> R.string.account_format_error
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
            !Pattern.matches(REGEXP_PASSWORD, pw) -> R.string.password_format_error_1
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
            !Pattern.matches(REGEXP_PASSWORD, pw) -> R.string.password_format_error_1
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
            !Pattern.matches(REGEXP_PASSWORD, pw) -> R.string.password_format_error_1
            else -> null
        }

        return when (_loginPasswordError.value) {
            null -> true
            else -> false
        }
    }

    private fun doLogin(account: String, password: String) {
//        viewModelScope.launch {
//            accountManager.login(account, password)
//                .collect { _loginResult.value = it }
//        }
    }
}