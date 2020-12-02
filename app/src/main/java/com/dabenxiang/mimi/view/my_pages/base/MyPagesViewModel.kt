package com.dabenxiang.mimi.view.my_pages.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.view.base.BaseViewModel

class MyPagesViewModel : BaseViewModel() {

    private val _deleteAll = MutableLiveData<Int>()
    val deleteAll: LiveData<Int> = _deleteAll

    open fun setDeleteNotify(tabIndex:Int){
        _deleteAll.value = tabIndex
    }

}
