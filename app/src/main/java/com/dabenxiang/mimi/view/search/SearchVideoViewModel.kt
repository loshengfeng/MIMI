package com.dabenxiang.mimi.view.search

import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.EditTextLiveData
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData

class SearchVideoViewModel : BaseViewModel() {

    private val _searchTextLiveData = EditTextMutableLiveData()
    val searchTextLiveData: EditTextLiveData = _searchTextLiveData

    fun cleanSearchText() {
        _searchTextLiveData.value = ""
    }
}