package com.dabenxiang.mimi.view.updateprofile

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.model.api.vo.ProfileRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UpdateProfileViewModel : BaseViewModel() {

    lateinit var profileItem: ProfileItem

    var type = UpdateProfileFragment.TYPE_NAME
    //FIXME !!!  use the same content !!!!
    val content = EditTextMutableLiveData()
    val birthday = EditTextMutableLiveData()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _updateResult = MutableLiveData<ApiResult<Nothing>>()
    val updateResult: LiveData<ApiResult<Nothing>> = _updateResult

    fun doRegisterValidateAndSubmit() {
        when (type) {
            UpdateProfileFragment.TYPE_NAME -> _error.value = isValidateFriendlyName()
            UpdateProfileFragment.TYPE_EMAIL -> _error.value = isValidateEmail()
            UpdateProfileFragment.TYPE_BIRTHDAY -> _error.value = isValidateBirthday()
            UpdateProfileFragment.TYPE_GEN -> _error.value = ""
        }
        if (_error.value == "") {
            updateProfile()
        }
    }

    private fun isValidateFriendlyName(): String {

        val name = content.value ?: ""
        return when {
            TextUtils.isEmpty(name) -> app.getString(R.string.login_name)
            !GeneralUtils.isFriendlyNameValid(name) -> app.getString(R.string.friendly_name_format_error_1)
            name.length > 20 -> app.getString(R.string.friendly_name_format_error_2)
            else -> {
                profileItem.friendlyName = name
                ""
            }
        }
    }

    private fun isValidateEmail(): String {
        val email = content.value ?: ""
        return when {
            TextUtils.isEmpty(email) -> app.getString(R.string.email_format_error_1)
            !GeneralUtils.isEmailValid(email) -> app.getString(R.string.email_format_error_2)
            email.length > 100 -> app.getString(R.string.email_format_error_3)
            else -> {
                profileItem.email = email
                ""
            }
        }
    }

    private fun isValidateBirthday(): String {
        val birthday = birthday.value ?: ""
        return when {
            TextUtils.isEmpty(birthday) -> app.getString(R.string.setting_type_birthday)
            birthday.length != 10 -> app.getString(R.string.setting_birthday_error)
            else -> {
                profileItem.birthday = birthday
                ""
            }
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            flow {
                val request = ProfileRequest(
                    profileItem.friendlyName,
                    profileItem.gender,
                    profileItem.birthday,
                    profileItem.email,
                    BuildConfig.API_HOST + DomainManager.VALIDATION_URL
                )
                val result = domainManager.getApiRepository().updateProfile(request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _updateResult.value = it }
        }
    }
}