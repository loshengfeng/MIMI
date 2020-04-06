package com.dabenxiang.mimi.view.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    val gson: Gson by inject()
//    val toastData = MutableLiveData<String>()
//    val dialogData = MutableLiveData<String>()
    val navigateView = MutableLiveData<Int>()
//    val isShowProgress = MutableLiveData<Boolean>().also { it.value = false }
}
