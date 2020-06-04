package com.dabenxiang.mimi.view.login

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MembersAccountItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.AppUtils
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isFriendlyNameValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isPasswordValid
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class LoginViewModel : BaseViewModel() {
    private val app: Application by inject()
    private val apiRepository: ApiRepository by inject()

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

    // Login
    private val _loginAccountError = MutableLiveData<String>()
    val loginAccountError: LiveData<String> = _loginAccountError

    private val _loginPasswordError = MutableLiveData<String>()
    val loginPasswordError: LiveData<String> = _loginPasswordError

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _apiResult = MutableLiveData<ApiResult<Nothing>>()
    val apiResult: LiveData<ApiResult<Nothing>> = _apiResult

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
            Timber.d("James_success")
//            viewModelScope.launch {
//                flow {
//                    val resp = apiRepository.signUp(
//                        MembersAccountItem(
//                            username = registerAccount.value,
//                            email = email.value,
//                            friendlyName = friendlyName.value,
//                            password = registerPw.value,
//                            promoCode = "TestCode",
//                            // TODO: 從DomainManager取得Url
//                            validationUrl = BuildConfig.API_HOST + "v1/Members/ValidateEmail"
//                        )
//                    )
//                    if (!resp.isSuccessful) throw HttpException(resp)
//                    emit(ApiResult.success(null))
//                }
//                    .flowOn(Dispatchers.IO)
//                    .onStart { emit(ApiResult.loading()) }
//                    .onCompletion { emit(ApiResult.loaded()) }
//                    .catch { e -> emit(ApiResult.error(e)) }
//                    .collect { resp ->
//                        when (resp) {
//                            is ApiResult.Success -> {
//                                Timber.d("${LoginViewModel::class.java.simpleName}_ApiResult.success")
//                                _apiResult.value = resp
//                            }
//                            is ApiResult.Error -> {
//                                Timber.d("${LoginViewModel::class.java.simpleName}_ApiResult.error")
//                                when (resp.throwable) {
//                                    is HttpException -> {
//                                        val data = AppUtils.getHttpExceptionData(resp.throwable)
//                                        val errorItem = data.errorItem
//                                        Timber.d("${LoginViewModel::class.java.simpleName}_isHttpException")
//                                        Timber.d("${LoginViewModel::class.java.simpleName}_code: ${errorItem.code}")
//                                        Timber.d("${LoginViewModel::class.java.simpleName}_message: ${errorItem.message}")
//                                    }
//                                    else -> {
//                                        toastData.value = resp.throwable.toString()
//                                    }
//                                }
//                            }
//                            is ApiResult.Loading -> setShowProgress(true)
//                            is ApiResult.Loaded -> setShowProgress(false)
//                        }
//                    }
//            }
        }
    }

    fun doLoginValidateAndSubmit() {
        _loginAccountError.value = isValidateAccount(loginAccount.value ?: "")
        _loginPasswordError.value = isValidatePassword(loginPw.value ?: "")

        if ("" == _loginAccountError.value &&
            "" == _loginPasswordError.value
        ) {
            loginPw.value?.let { loginAccount.value?.let { it1 -> doLogin(it1, it) } }
        }
    }

    private fun doLogin(userName: String, password: String) {
        viewModelScope.launch {
            accountManager.signIn(userName, password).collect {
                _apiResult.value = it
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
            // todo: email_format_error_4
            else -> ""
        }
    }

    private fun isValidateAccount(account: String): String {
        return when {
            TextUtils.isEmpty(account) -> app.getString(R.string.account_format_error_1)
            !isAccountValid(account) -> app.getString(R.string.account_format_error_2)
            // todo: account_format_error_3
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
}