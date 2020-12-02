package com.dabenxiang.mimi.view.my_pages.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.view.base.BaseViewModel

abstract class MyPagesViewModel : BaseViewModel() {

    private val _deleteAll = MutableLiveData<Int>()
    val deleteAll: LiveData<Int> = _deleteAll

    fun setDeleteNotify(tabIndex:Int){
        _deleteAll.value = tabIndex
    }

}
