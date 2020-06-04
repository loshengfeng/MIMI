package com.dabenxiang.mimi.view.forgetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import timber.log.Timber
import java.util.regex.Pattern

class ForgetPasswordViewModel : BaseViewModel() {
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

    private fun isValidateAccount(account: String): Boolean {
        _accountError.value = when {
            account.isNullOrBlank() -> R.string.account_format_error_1
            !isAccountValid(account) -> R.string.account_format_error_2
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
            !isEmailValid(email) -> R.string.email_format_error_2
            else -> null
        }

        return when (_emailError.value) {
            null -> true
            else -> false
        }
    }

    private fun doReset(account: String, password: String) {
        toastData.value = "doReset"
//        viewModelScope.launch {
//            accountManager.reset(account, password)
//                .collect { _result.value = it }
//        }
    }
}