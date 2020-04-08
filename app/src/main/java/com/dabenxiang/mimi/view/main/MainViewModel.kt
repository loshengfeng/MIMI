package com.dabenxiang.mimi.view.main

import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.view.base.BaseViewModel

class MainViewModel : BaseViewModel() {

    val enableNightMode = MutableLiveData<Boolean>()
}