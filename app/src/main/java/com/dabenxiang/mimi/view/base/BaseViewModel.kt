package com.dabenxiang.mimi.view.base

import android.accounts.AccountManager
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val accountManager: AccountManager by inject()
    val gson: Gson by inject()
//    val toastData = MutableLiveData<String>()
//    val dialogData = MutableLiveData<String>()
//    val isShowProgress = MutableLiveData<Boolean>().also { it.value = false }
}
