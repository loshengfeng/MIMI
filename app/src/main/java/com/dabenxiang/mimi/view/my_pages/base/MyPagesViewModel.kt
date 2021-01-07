package com.dabenxiang.mimi.view.my_pages.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.view.base.BaseViewModel
import timber.log.Timber

class MyPagesViewModel : BaseViewModel() {

    private val _deleteAll = MutableLiveData<Int>()
    val deleteAll: LiveData<Int> = _deleteAll

    private val _changeDataIsEmpty = MutableLiveData<Pair<Int, Boolean>>()
    val changeDataIsEmpty: LiveData<Pair<Int, Boolean>> = _changeDataIsEmpty

    open fun setDeleteNotify(tabIndex: Int) {
        _deleteAll.value = tabIndex
    }

    fun changeDataIsEmpty(tabIndex: Int, isEmpty: Boolean) {
        Timber.d("changeIsEmpty($tabIndex): $isEmpty")
        _changeDataIsEmpty.value = Pair(tabIndex, isEmpty)
    }

}
