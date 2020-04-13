package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.view.base.BaseViewModel
import org.koin.core.inject

class HomeViewModel : BaseViewModel() {

    private val apiRepository: ApiRepository by inject()

    private val mTabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = mTabLayoutPosition

    fun setTopTabPosition(position: Int) {
        mTabLayoutPosition.value = position
    }
}