package com.dabenxiang.mimi.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MembersAccountItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.AppUtils
import com.dabenxiang.mimi.widget.utility.AppUtils.isAccountValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isEmailValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isFriendlyNameValid
import com.dabenxiang.mimi.widget.utility.AppUtils.isPasswordValid
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class LoginViewModel : BaseViewModel() {
    private val apiRepository: ApiRepository by inject()

    var type = TYPE_REGISTER

    val registerAccount = EditTextMutableLiveData()
    val email = EditTextMutableLiveData()
    val registerPw = EditTextMutableLiveData()
    val friendlyName = EditTextMutableLiveData()
    val confirmPw = EditTextMutableLiveData()

    val loginAccount = EditTextMutableLiveData()
    val loginPw = EditTextMutableLiveData()

    private val _registerAccountError = MutableLiveData<Int>()
    val registerAccountError: LiveData<Int> = _registerAccountError

    private val _loginAccountError = MutableLiveData<Int>()
    val loginAccountError: LiveData<Int> = _loginAccountError

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int> = _emailError

    private val _friendlyNameError = MutableLiveData<Int>()
    val friendlyNameError: LiveData<Int> = _friendlyNameError

    private val _loginPasswordError = MutableLiveData<Int>()
    val loginPasswordError: LiveData<Int> = _loginPasswordError

    private val _registerPasswordError = MutableLiveData<Int>()
    val registerPasswordError: LiveData<Int> = _registerPasswordError

    private val _confirmPasswordError = MutableLiveData<Int>()
    val confirmPasswordError: LiveData<Int> = _confirmPasswordError

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _apiSignUpResult = MutableLiveData<ApiResult<Nothing>>()
    val apiSignUpResult: LiveData<ApiResult<Nothing>> = _apiSignUpResult

    fun doRegisterValidateAndSubmit() {
        val account = registerAccount.value ?: ""
        val email = email.value ?: ""
        val friendlyName = friendlyName.value ?: ""
        val registerPw = registerPw.value ?: ""
        val confirmPw = confirmPw.value ?: ""

        if (isValidateAccount(account) &&
            isValidateEmail(email) &&
            isValidateFriendlyName(friendlyName) &&
            isValidatePassword(registerPw) &&
            isValidateConfirmPassword(registerPw, confirmPw)) {
            viewModelScope.launch {
                    flow {
                        val resp = apiRepository.signUp(
                            MembersAccountItem(
                                account,
                                email,
                                friendlyName,
                                registerPw,
                                confirmPw
                            )
                        )

                        if (!resp.isSuccessful) throw HttpException(resp)

                        emit(ApiResult.success(null))
                    }
                        .flowOn(Dispatchers.IO)
                        .onStart { emit(ApiResult.loading()) }
                        .onCompletion { emit(ApiResult.loaded()) }
                        .catch { e -> emit(ApiResult.error(e)) }
                        .collect { resp ->
                            when (resp) {
                                is ApiResult.Success -> {
                                    Timber.d("${LoginViewModel::class.java.simpleName}_ApiResult.success")
                                    _apiSignUpResult.value = resp
                                }
                                is ApiResult.Error -> {
                                    Timber.d("${LoginViewModel::class.java.simpleName}_ApiResult.error")
                                    when(resp.throwable) {
                                        is HttpException -> {
                                            val data = AppUtils.getHttpExceptionData(resp.throwable)
                                            val errorItem = data.errorItem
                                            Timber.d("${LoginViewModel::class.java.simpleName}_isHttpException")
                                            Timber.d("${LoginViewModel::class.java.simpleName}_code: ${errorItem.code}")
                                            Timber.d("${LoginViewModel::class.java.simpleName}_message: ${errorItem.message}")
                                        }
                                        else -> {
                                            toastData.value = resp.throwable.toString()
//                                            Timber.d("${LoginViewModel::class.java.simpleName}_code: ${resp.throwable}")
//                                            Timber.d("${LoginViewModel::class.java.simpleName}_message: ${resp.message()}")
                                        }
                                    }
//                                    Timber.e(resp.throwable)
                                }
                                is ApiResult.Loading -> setShowProgress(true)
                                is ApiResult.Loaded -> setShowProgress(false)
                            }
                        }
            }
        }
    }

    fun doLoginValidateAndSubmit() {
        val account = loginAccount.value ?: ""
        val pw = loginPw.value ?: ""
        if (isValidateAccount(account) &&
            isValidatePassword(pw)) {
            doLogin(account, pw)
        }
    }

    private fun isValidateAccount(account: String): Boolean {
        when (type) {
            TYPE_REGISTER -> {
                _registerAccountError.value = when {
                    account.isNullOrBlank() -> R.string.account_empty
                    !isAccountValid(account) -> R.string.account_format_error
                    else -> null
                }

                return when (_registerAccountError.value) {
                    null -> true
                    else -> false
                }
            }
            TYPE_LOGIN -> {
                _loginAccountError.value = when {
                    account.isNullOrBlank() -> R.string.account_empty
                    !isAccountValid(account) -> R.string.account_format_error
                    else -> null
                }

                return when (_loginAccountError.value) {
                    null -> true
                    else -> false
                }
            }
            else -> return true
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

    private fun isValidateFriendlyName(name: String): Boolean {
        _friendlyNameError.value = when {
            name.isNullOrBlank() -> R.string.friendly_name_format_error_1
            !isFriendlyNameValid(name) -> R.string.friendly_name_format_error_2
            else -> null
        }

        return when (_friendlyNameError.value) {
            null -> true
            else -> false
        }
    }

    private fun isValidatePassword(pw: String): Boolean {
        when (type) {
            TYPE_REGISTER -> {
                _registerPasswordError.value = when {
                    pw.isNullOrBlank() -> R.string.password_empty
                    !isPasswordValid(pw) -> R.string.password_format_error_1
                    else -> null
                }

                return when (_registerPasswordError.value) {
                    null -> true
                    else -> false
                }
            }
            TYPE_LOGIN -> {
                _loginPasswordError.value = when {
                    pw.isNullOrBlank() -> R.string.password_empty
                    !isPasswordValid(pw) -> R.string.password_format_error_1
                    else -> null
                }

                return when (_loginPasswordError.value) {
                    null -> true
                    else -> false
                }
            }
            else -> return false
        }
    }

    private fun isValidateConfirmPassword(pw: String, confirmPw: String): Boolean {
        _confirmPasswordError.value = when {
            pw.isNullOrBlank() -> R.string.password_empty
            !isPasswordValid(pw) -> R.string.password_format_error_1
            pw != confirmPw -> R.string.password_format_error_2
            else -> null
        }

        return when (_confirmPasswordError.value) {
            null -> true
            else -> false
        }
    }

    private fun doLogin(account: String, password: String) {
        toastData.value = "doLogin"
//        viewModelScope.launch {
//            accountManager.login(account, password)
//                .collect { _loginResult.value = it }
//        }
    }
}