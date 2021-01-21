package com.dabenxiang.mimi.view.login

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BindPhoneRequest
import com.dabenxiang.mimi.model.api.vo.ValidateMessageRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
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
    var clickType = TYPE_REGISTER

    var clickTime: Long = 0

    var changePrefixCount = 0
    var changePWDCount = 0 // 登入使用 password
    var mobileValidCount = 0
    var timer: Timer? = null

    var isNeedValidMobile = false

    val account = EditTextMutableLiveData()
    val registerPw = EditTextMutableLiveData()
    val confirmPw = EditTextMutableLiveData()

    val loginAccount = EditTextMutableLiveData()
    val loginPw = EditTextMutableLiveData()
    val mobile = EditTextMutableLiveData()
    val verificationCode = EditTextMutableLiveData()
    val loginVerificationCode = EditTextMutableLiveData()
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

    private val _invitedCodeError = MutableLiveData<String>()
    val invitedCodeError: LiveData<String> = _invitedCodeError

    // Login
    private val _loginAccountError = MutableLiveData<String>()
    val loginAccountError: LiveData<String> = _loginAccountError

    private val _loginPasswordError = MutableLiveData<String>()
    val loginPasswordError: LiveData<String> = _loginPasswordError

    private val _loginVerificationCodeError = MutableLiveData<String>()
    val loginVerificationCodeError: LiveData<String> = _loginVerificationCodeError

    private val _loginResult = MutableLiveData<ApiResult<Nothing>>()
    val loginResult: LiveData<ApiResult<Nothing>> = _loginResult

    private val _validateMessageResult = MutableLiveData<ApiResult<Nothing>>()
    val validateMessageResult: LiveData<ApiResult<Nothing>> = _validateMessageResult

    private val _registerExistResult = MutableLiveData<ApiResult<Nothing>>()
    val registerExistResult: LiveData<ApiResult<Nothing>> = _registerExistResult

    private val _loginExistResult = MutableLiveData<ApiResult<Nothing>>()
    val loginExistResult: LiveData<ApiResult<Nothing>> = _loginExistResult

    private val _loginMobileErrorResult = MutableLiveData<String>()
    val loginMobileErrorResult: LiveData<String> = _loginMobileErrorResult

    /**
     * 註冊邏輯
     */
    fun doRegisterValidateAndSubmit(callPrefix: String) {
        clickType = TYPE_REGISTER
        _accountError.value = isValidateFriendlyName(account.value ?: "")
        _mobileError.value = isValidateMobile(mobile.value ?: "", callPrefix)
//        _registerAccountError.value = isValidateAccount(registerAccount.value ?: "")
        _validateCodeError.value = isValidateValidateCode(verificationCode.value ?: "")
//        _registerPasswordError.value = isValidatePassword(registerPw.value ?: "")
//        _confirmPasswordError.value =
//            isValidateConfirmPassword(registerPw.value ?: "", confirmPw.value ?: "")

        if ("" == _accountError.value &&
                "" == _mobileError.value &&
//            "" == _registerPasswordError.value &&
//            "" == _confirmPasswordError.value &&
                "" == _validateCodeError.value
        ) {
            viewModelScope.launch {
                accountManager.bindPhone(
                    BindPhoneRequest(
                                username = callPrefix + mobile.value,
                                friendlyName = account.value,
                                referrerCode = inviteCode.value,
                                code = verificationCode.value
                        )
                ).collect {
                    _registerResult.value = it
                }
            }
        }
    }

    /**
     * 登入邏輯
     */
    fun doLoginValidateAndSubmit(callPrefix: String, isPwd: Boolean) {
        clickType = TYPE_LOGIN
        _loginAccountError.value = isValidateMobile(loginAccount.value ?: "", callPrefix)
        _loginVerificationCodeError.value = isValidateValidateCode(loginVerificationCode.value ?: "")

        if (isPwd) {
            _loginPasswordError.value = isValidatePassword(loginPw.value ?: "")
        }

        if ("" == _loginAccountError.value  && "" == _loginVerificationCodeError.value) {

            val account: String = loginAccount.value ?: ""
            val pwd: String = if (isPwd) loginPw.value ?: "" else ""
            val code: String = loginVerificationCode.value ?: ""

            doLogin(callPrefix + account, password = pwd, code = code)
        }
    }

    fun doLogin(userName: String, password: String = "", code: String = "") {
        viewModelScope.launch {
//            accountManager.authSignIn(userName, password, code)
//                    .collect { _loginResult.value = it }
            accountManager.signIn(pref.profileItem.userId, userName, code)
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
            isMobileValid(callPrefix, mobile, isNeedValidMobile) -> app.getString(R.string.mobile_format_error_2)
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

    fun validateCodeError(s: Int) {
        _validateCodeError.value = app.getString(s)
    }

    fun callRegisterIsMemberExist(callPrefix: String, phoneNumber: String) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.isMemberExist(callPrefix + phoneNumber)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _registerExistResult.value = it }
        }
    }

    fun callLoginIsMemberExist(callPrefix: String, phoneNumber: String) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.isMemberExist(callPrefix + phoneNumber)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _loginExistResult.value = it }
        }
    }

    fun callValidateMessage(callPrefix: String, phoneNumber: String) {
        viewModelScope.launch {
            flow {
                val body = ValidateMessageRequest(callPrefix + phoneNumber)

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

    fun onMobileError(msg: String) {
        _mobileError.value = msg
    }

    fun onResetMobileError() {
        _mobileError.value = ""
    }

    fun onInvitedCodeError(msg: String) {
        _invitedCodeError.value = msg
    }

    fun onLoginMobileError(msg: String) {
        _loginMobileErrorResult.value = msg
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