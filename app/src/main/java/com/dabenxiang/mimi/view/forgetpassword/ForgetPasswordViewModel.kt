package com.dabenxiang.mimi.view.forgetpassword

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ApiResult.Companion.loaded
import com.dabenxiang.mimi.model.api.ApiResult.Companion.loading
import com.dabenxiang.mimi.model.api.vo.ForgetPasswordRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ForgetPasswordViewModel : BaseViewModel() {
    val account = EditTextMutableLiveData()
    val email = EditTextMutableLiveData()

    private val _accountError = MutableLiveData<String>()
    val accountError: LiveData<String> = _accountError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> = _emailError

    private val _result = MutableLiveData<ApiResult<Nothing>>()
    val result: LiveData<ApiResult<Nothing>> = _result

    fun doValidateAndSubmit() {
        _accountError.value = isValidateAccount(account.value ?: "")
        _emailError.value = isValidateEmail(email.value ?: "")

        if ("" == _accountError.value && "" == _emailError.value) {
            account.value?.let { it1 -> email.value?.let { it2 -> doReset(it1, it2) }}
        }
    }

    private fun isValidateAccount(account: String): String {
        return when {
            TextUtils.isEmpty(account) -> app.getString(R.string.account_format_error_1)
            !isAccountValid(account) -> app.getString(R.string.account_format_error_2)
            else -> ""
        }
    }

    private fun isValidateEmail(email: String): String {
        return when {
            TextUtils.isEmpty(email) -> app.getString(R.string.email_format_error_1)
            !isEmailValid(email) -> app.getString(R.string.email_format_error_2)
            email.length > 100 -> app.getString(R.string.email_format_error_3)
            else -> ""
        }
    }

    private fun doReset(account: String, email: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().forgetPassword(ForgetPasswordRequest(account, email))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(loaded()) }
                .collect {
                    _result.value = it
                }
        }
    }
}