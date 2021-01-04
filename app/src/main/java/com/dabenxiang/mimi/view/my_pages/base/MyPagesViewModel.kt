package com.dabenxiang.mimi.view.my_pages.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.view.base.BaseViewModel

class MyPagesViewModel : BaseViewModel() {

    private val _deleteAll = MutableLiveData<Int>()
    val deleteAll: LiveData<Int> = _deleteAll

    private val _changeDataCount = MutableLiveData<Pair<Int, Int>>()
    val changeDataCount: LiveData<Pair<Int, Int>> = _changeDataCount

    open fun setDeleteNotify(tabIndex: Int) {
        _deleteAll.value = tabIndex
    }

    fun changeDataCount(tabIndex: Int, count: Int) {
        _changeDataCount.value = Pair(tabIndex, count)
    }

}
