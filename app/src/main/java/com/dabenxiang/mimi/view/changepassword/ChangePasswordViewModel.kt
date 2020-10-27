package com.dabenxiang.mimi.view.changepassword

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChangePasswordViewModel : BaseViewModel() {

    val current = EditTextMutableLiveData()
    val new = EditTextMutableLiveData()
    val confirm = EditTextMutableLiveData()

    private val _currentError = MutableLiveData<String>()
    val currentError: LiveData<String> = _currentError

    private val _newError = MutableLiveData<String>()
    val newError: LiveData<String> = _newError

    private val _confirmError = MutableLiveData<String>()
    val confirmError: LiveData<String> = _confirmError

    private val _changeResult = MutableLiveData<ApiResult<Nothing>>()
    val changeResult: LiveData<ApiResult<Nothing>> = _changeResult

    fun doLoginValidateAndSubmit() {
        _currentError.value = isValidateCurrent(current.value ?: "")
        _newError.value = isValidateNew(new.value ?: "")
        _confirmError.value = isValidateConfirm(new.value ?: "", confirm.value ?: "")

        if (_currentError.value == "" &&
            _newError.value == "" &&
            _confirmError.value == ""
        ) {
            current.value?.let { new.value?.let { it1 -> doChangePwd(it, it1) } }
        }
    }

    private fun isValidateCurrent(pwd: String): String {
        return when {
            TextUtils.isEmpty(pwd) -> app.getString(R.string.setting_current_password_error_1)
            else -> ""
        }
    }

    private fun isValidateNew(pwd: String): String {
        return when {
            TextUtils.isEmpty(pwd) -> app.getString(R.string.setting_new_password_error_1)
            !GeneralUtils.isPasswordValid(pwd) -> app.getString(R.string.password_format_error_2)
            else -> ""
        }
    }

    private fun isValidateConfirm(pwd: String, confirmPw: String): String {
        return when {
            TextUtils.isEmpty(confirmPw) -> app.getString(R.string.setting_confirm_password_error_1)
            pwd != confirmPw -> app.getString(R.string.setting_confirm_password_error_2)
            else -> ""
        }
    }

    private fun doChangePwd(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            accountManager.changePwd(oldPassword, newPassword)
                .collect { _changeResult.value = it }
        }
    }
}