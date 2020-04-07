package com.dabenxiang.mimi.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import java.util.regex.Pattern

class LoginViewModel : BaseViewModel() {

    companion object {
        const val TYPE_REGISTER = 0
        const val TYPE_LOGIN = 1
        const val REGEXP_USER_NAME = "^[a-zA-Z0-9]{5,20}$"
        const val REGEXP_PASSWORD = "^\\S{8,20}\$"
        const val REGEXP_EMAIL = "^[A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$"
    }

//    private val _accountError = MutableLiveData<Int>()
//    val accountError: LiveData<Int> = _accountError

    private val _accountError = MutableLiveData<Map<Int, Int>>()
    val accountError: LiveData<Map<Int, Int>> = _accountError

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
        if (isValidateAccount(account, TYPE_REGISTER) &&
            isValidateEmail(email) &&
            isValidateRegisterPassword(pw) &&
            isValidateConfirmPassword(pw, confirmPw)) {
            doRegister(account, confirmPw)
        }
    }

    fun doLoginValidateAndSubmit(account: String, password: String) {
        if (isValidateAccount(account, TYPE_LOGIN) &&
            isValidateLoginPassword(password)) {
            doLogin(account, password)
        }
    }

    private fun isValidateAccount(userName: String, type: Int): Boolean {
        // todo
        /*val result = when {
            userName.isNullOrBlank() -> R.string.account_empty
            !Pattern.matches(REGEXP_USER_NAME, userName) -> R.string.account_format_error
            else -> 0
        }

        _accountError.value = mapOf(type to result)

        return when (result) {
            0 -> true
            else -> false
        }*/
        val result = when {
            userName.isNullOrBlank() -> R.string.account_empty
            !Pattern.matches(REGEXP_USER_NAME, userName) -> R.string.account_format_error
            else -> 0
        }

        _accountError.value = mapOf(type to result)

        return when (result) {
            0 -> true
            else -> false
        }
    }

    private fun isValidateEmail(email: String): Boolean {
        _emailError.value = when {
            email.isNullOrBlank() -> R.string.email_format_error_1
            !Pattern.matches(REGEXP_EMAIL, email) -> R.string.email_format_error_2
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

    private fun doRegister(account: String, password: String) {
//        viewModelScope.launch {
//            accountManager.register(account, password)
//                .collect { _loginResult.value = it }
//        }
    }
}