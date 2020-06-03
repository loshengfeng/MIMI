package com.dabenxiang.mimi.view.personal


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PersonalViewModel : BaseViewModel() {

    private val _apiSignOut = MutableLiveData<ApiResult<Nothing>>()
    val apiSignOut: LiveData<ApiResult<Nothing>> = _apiSignOut

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut().collect {
                _apiSignOut.value = it
            }
        }
    }
}