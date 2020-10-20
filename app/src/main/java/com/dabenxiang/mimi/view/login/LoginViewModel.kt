package com.dabenxiang.mimi.view.login

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.SingUpRequest
import com.dabenxiang.mimi.model.api.vo.ValidateMessageRequest
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isFriendlyNameValid
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isMobileValid
import com.dabenxiang.mimi.widget.utility.GeneralUtils.isPasswordValid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.*
import kotlin.concurrent.schedule

@ExperimentalCoroutinesApi
class LoginViewModel : BaseViewModel() {
    var type = TYPE_REGISTER

    var changePrefixCount = 0
    var timer: Timer? = null

    val account = EditTextMutableLiveData()
    val registerPw = EditTextMutableLiveData()
    val confirmPw = EditTextMutableLiveData()

    val loginAccount = EditTextMutableLiveData()
    val loginPw = EditTextMutableLiveData()
    val mobile = EditTextMutableLiveData()
    val verificationCode = EditTextMutableLiveData()
    val inviteCode = EditTextMutableLiveData()

    // Register
    private val _mobileError = MutableLiveData<String>()
    val mobileError: LiveData<String> = _mobileError

    private val _registerPasswordError = MutableLiveData<String>()
    val registerPasswordError: LiveData<String> = _registerPasswordError

    private val _confirmPasswordError = MutableLiveData<String>()
    val confirmPasswordError: LiveData<String> = _confirmPasswordError

    private val _validateCodeError = MutableLiveData<String>()
    val validateCodeError: LiveData<String> = _validateCodeError

    private val _accountError = MutableLiveData<String>()
    val accountError: LiveData<String> = _accountError

    private val _registerResult = MutableLiveData<ApiResult<Nothing>>()
    val registerResult: LiveData<ApiResult<Nothing>> = _registerResult

    // Login
    private val _loginAccountError = MutableLiveData<String>()
    val loginAccountError: LiveData<String> = _loginAccountError

    private val _loginPasswordError = MutableLiveData<String>()
    val loginPasswordError: LiveData<String> = _loginPasswordError

    private val _loginResult = MutableLiveData<ApiResult<Nothing>>()
    val loginResult: LiveData<ApiResult<Nothing>> = _loginResult

    private val _validateMessageResult = MutableLiveData<ApiResult<Nothing>>()
    val validateMessageResult: LiveData<ApiResult<Nothing>> = _validateMessageResult

    fun doRegisterValidateAndSubmit(callPrefix: String) {
        _accountError.value = isValidateFriendlyName(account.value ?: "")
        _mobileError.value = isValidateMobile(mobile.value ?: "", callPrefix)
//        _registerAccountError.value = isValidateAccount(registerAccount.value ?: "")
        _validateCodeError.value = isValidateValidateCode(verificationCode.value ?: "")
        _registerPasswordError.value = isValidatePassword(registerPw.value ?: "")
        _confirmPasswordError.value =
            isValidateConfirmPassword(registerPw.value ?: "", confirmPw.value ?: "")

        if ("" == _accountError.value &&
            "" == _mobileError.value &&
            "" == _registerPasswordError.value &&
            "" == _confirmPasswordError.value &&
            "" == _validateCodeError.value
        ) {
            viewModelScope.launch {
                accountManager.signUp(
                    SingUpRequest(
                        username = callPrefix + mobile.value,
                        friendlyName = account.value,
                        password = registerPw.value,
                        referrerCode = inviteCode.value,
                        code = verificationCode.value
                    )
                ).collect {
                    _registerResult.value = it
                }
            }
        }
    }

    fun doLoginValidateAndSubmit(callPrefix: String) {
        _loginAccountError.value = isValidateAccount(loginAccount.value ?: "")
        _loginPasswordError.value = isValidatePassword(loginPw.value ?: "")

        if ("" == _loginAccountError.value &&
            "" == _loginPasswordError.value
        ) {
            loginAccount.value?.let { loginPw.value?.let { it1 -> doLogin(callPrefix + it, it1) } }
        }
    }

    fun doLogin(userName: String, password: String) {
        viewModelScope.launch {
            accountManager.signIn(userName, password)
                .collect { _loginResult.value = it }
        }
    }

    private fun isValidateFriendlyName(name: String): String {
        return when {
            TextUtils.isEmpty(name) -> app.getString(R.string.login_name)
            !isFriendlyNameValid(name) -> app.getString(R.string.friendly_name_format_error_1)
            name.length > 20 -> app.getString(R.string.friendly_name_format_error_2)
            else -> ""
        }
    }

    fun isValidateMobile(mobile: String, callPrefix: String): String {
        return when {
            TextUtils.isEmpty(mobile) -> app.getString(R.string.mobile_format_error_1)
            isMobileValid(callPrefix, mobile) -> app.getString(R.string.mobile_format_error_1)
            else -> ""
        }
    }

    private fun isValidateValidateCode(mobile: String): String {
        return when {
            TextUtils.isEmpty(mobile) -> app.getString(R.string.invalidate_code_error_1)
            else -> ""
        }
    }

    private fun isValidateAccount(account: String): String {
        return when {
            TextUtils.isEmpty(account) -> app.getString(R.string.mobile_format_error_1)
            else -> ""
        }
    }

    private fun isValidatePassword(pwd: String): String {
        return when {
            TextUtils.isEmpty(pwd) -> app.getString(R.string.password_format_error_1)
            !isPasswordValid(pwd) -> app.getString(R.string.password_format_error_2)
            else -> ""
        }
    }

    private fun isValidateConfirmPassword(pwd: String, confirmPw: String): String {
        return when {
            TextUtils.isEmpty(confirmPw) -> app.getString(R.string.password_format_error_3)
            pwd != confirmPw -> app.getString(R.string.password_format_error_4)
            else -> ""
        }
    }

    fun callValidateMessage(callPrefix: String) {
        viewModelScope.launch {
            flow {
                val body = ValidateMessageRequest(callPrefix + mobile.value)

                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.validateMessage(body)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _validateMessageResult.value = it }
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