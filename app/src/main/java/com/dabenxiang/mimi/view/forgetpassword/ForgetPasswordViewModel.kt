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
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isAccountValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.*
import kotlin.concurrent.schedule

class ForgetPasswordViewModel : BaseViewModel() {

    var changePrefixCount = 0
    var timer: Timer? = null

    val account = EditTextMutableLiveData()
    val mobile = EditTextMutableLiveData()

    private val _mobileError = MutableLiveData<String>()
    val mobileError: LiveData<String> = _mobileError

    private val _accountError = MutableLiveData<String>()
    val accountError: LiveData<String> = _accountError

    private val _result = MutableLiveData<ApiResult<Nothing>>()
    val result: LiveData<ApiResult<Nothing>> = _result

    fun doValidateAndSubmit(callPrefix: String) {
        _mobileError.value = isValidateMobile(mobile.value ?: "")
        _accountError.value = isValidateAccount(account.value ?: "")

        if ("" == _accountError.value && "" == _mobileError.value) {
            account.value?.let { it1 -> mobile.value?.let { it2 -> doReset(it1, it2) } }
        }
    }

    private fun isValidateAccount(account: String): String {
        return when {
            TextUtils.isEmpty(account) -> app.getString(R.string.account_error_1)
            !isAccountValid(account) -> app.getString(R.string.account_format_error_2)
            else -> ""
        }
    }

    private fun isValidateMobile(mobile: String): String {
        return when {
            TextUtils.isEmpty(mobile) -> app.getString(R.string.mobile_format_error_1)
            else -> ""
        }
    }

    private fun doReset(account: String, email: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .forgetPassword(ForgetPasswordRequest(account, email))
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

    fun startTimer() {
        timer = Timer()
        timer?.schedule(10000) {
            changePrefixCount = 0
            timer?.cancel()
            timer = null
            Timber.d("timer cancel")
        }
    }
}