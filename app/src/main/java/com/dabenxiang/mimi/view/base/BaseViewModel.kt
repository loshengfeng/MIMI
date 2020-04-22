package com.dabenxiang.mimi.view.base

import android.accounts.AccountManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val accountManager: AccountManager by inject()
    val gson: Gson by inject()
    val toastData = MutableLiveData<String>()
//    val dialogData = MutableLiveData<String>()

    private val _showProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _navigateDestination by lazy { MutableLiveData<NavigateItem>() }
    val navigateDestination: LiveData<NavigateItem> = _navigateDestination

    fun setShowProgress(show: Boolean) {
        _showProgress.value = show
    }
}
