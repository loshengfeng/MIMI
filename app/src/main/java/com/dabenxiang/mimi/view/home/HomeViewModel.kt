package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class HomeViewModel : BaseViewModel2() {

    private val apiRepository: ApiRepository by inject()

    private val mTabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = mTabLayoutPosition

    fun setTopTabPosition(position: Int) {
        mTabLayoutPosition.value = position
    }
}