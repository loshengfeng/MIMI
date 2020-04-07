package com.dabenxiang.mimi.view.forgetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import timber.log.Timber
import java.util.regex.Pattern

class ForgetPasswordViewModel : BaseViewModel() {

    companion object {
        const val REGEXP_USER_NAME = "^[a-zA-Z0-9]{5,20}$"
        const val REGEXP_EMAIL = "^[A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$"
    }

    private val _accountError = MutableLiveData<Int>()
    val accountError: LiveData<Int> = _accountError

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int> = _emailError

    private val _result = MutableLiveData<ApiResult<Nothing>>()
    val result: LiveData<ApiResult<Nothing>> = _result

    fun doValidateAndSubmit(account: String, email: String) {
        Timber.d("${ForgetPasswordViewModel::class.java.simpleName}_send_account: $account, email: $email")
        if (isValidateAccount(account) &&
            isValidateEmail(email)) {
            doReset(account, email)
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

    private fun doReset(account: String, password: String) {
//        viewModelScope.launch {
//            accountManager.reset(account, password)
//                .collect { _result.value = it }
//        }
    }
}