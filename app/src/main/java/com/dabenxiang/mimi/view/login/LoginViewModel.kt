package com.dabenxiang.mimi.view.login

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.manager.DomainManager.Companion.PROMO_CODE
import com.dabenxiang.mimi.manager.DomainManager.Companion.VALIDATION_URL
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.model.api.vo.SingUpRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isFriendlyNameValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isPasswordValid
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel : BaseViewModel() {
    var type = TYPE_REGISTER

    val friendlyName = EditTextMutableLiveData()
    val registerAccount = EditTextMutableLiveData()
    val email = EditTextMutableLiveData()
    val registerPw = EditTextMutableLiveData()
    val confirmPw = EditTextMutableLiveData()

    val loginAccount = EditTextMutableLiveData()
    val loginPw = EditTextMutableLiveData()

    // Register
    private val _friendlyNameError = MutableLiveData<String>()
    val friendlyNameError: LiveData<String> = _friendlyNameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> = _emailError

    private val _registerAccountError = MutableLiveData<String>()
    val registerAccountError: LiveData<String> = _registerAccountError

    private val _registerPasswordError = MutableLiveData<String>()
    val registerPasswordError: LiveData<String> = _registerPasswordError

    private val _confirmPasswordError = MutableLiveData<String>()
    val confirmPasswordError: LiveData<String> = _confirmPasswordError

    private val _registerResult = MutableLiveData<ApiResult<Nothing>>()
    val registerResult: LiveData<ApiResult<Nothing>> = _registerResult

    // Login
    private val _loginAccountError = MutableLiveData<String>()
    val loginAccountError: LiveData<String> = _loginAccountError

    private val _loginPasswordError = MutableLiveData<String>()
    val loginPasswordError: LiveData<String> = _loginPasswordError

    private val _loginResult = MutableLiveData<ApiResult<Nothing>>()
    val loginResult: LiveData<ApiResult<Nothing>> = _loginResult

    private val _profileItem = MutableLiveData<ApiResult<ProfileItem>>()
    val profileItem : LiveData<ApiResult<ProfileItem>> = _profileItem

    @ExperimentalCoroutinesApi
    fun doRegisterValidateAndSubmit() {
        _friendlyNameError.value = isValidateFriendlyName(friendlyName.value ?: "")
        _emailError.value = isValidateEmail(email.value ?: "")
        _registerAccountError.value = isValidateAccount(registerAccount.value ?: "")
        _registerPasswordError.value = isValidatePassword(registerPw.value ?: "")
        _confirmPasswordError.value  = isValidateConfirmPassword(registerPw.value ?: "", confirmPw.value?: "")

        if ("" == _friendlyNameError.value &&
            "" == _emailError.value &&
            "" == _registerAccountError.value &&
            "" == _registerPasswordError.value &&
            "" == _confirmPasswordError.value
        ) {
            viewModelScope.launch {
                accountManager.singUp(
                    SingUpRequest(
                            username = registerAccount.value,
                            email = email.value,
                            friendlyName = friendlyName.value,
                            password = registerPw.value,
                            promoCode = PROMO_CODE,
                            validationUrl = BuildConfig.API_HOST + VALIDATION_URL
                        )
                ).collect {
                    _registerResult.value = it
                }
            }
        }
    }

    fun doLoginValidateAndSubmit() {
        _loginAccountError.value = isValidateAccount(loginAccount.value ?: "")
        _loginPasswordError.value = isValidatePassword(loginPw.value ?: "")

        if ("" == _loginAccountError.value &&
            "" == _loginPasswordError.value
        ) {
            loginAccount.value?.let { loginPw.value?.let { it1 -> doLogin(it, it1) } }
        }
    }

    fun doLogin(userName: String, password: String) {
        viewModelScope.launch {
            accountManager.signIn(userName, password).collect {
                _loginResult.value = it
            }
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

    private fun isValidateEmail(email: String): String {
        return when {
            TextUtils.isEmpty(email) -> app.getString(R.string.email_format_error_1)
            !isEmailValid(email) -> app.getString(R.string.email_format_error_2)
            email.length > 100 -> app.getString(R.string.email_format_error_3)
            else -> ""
        }
    }

    private fun isValidateAccount(account: String): String {
        return when {
            TextUtils.isEmpty(account) -> app.getString(R.string.account_format_error_1)
            !isAccountValid(account) -> app.getString(R.string.account_format_error_2)
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

    @ExperimentalCoroutinesApi
    fun getProfile() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getProfile()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _profileItem.value = it
                }
        }
    }
}