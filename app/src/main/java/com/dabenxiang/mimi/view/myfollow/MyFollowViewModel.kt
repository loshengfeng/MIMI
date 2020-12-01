package com.dabenxiang.mimi.view.myfollow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TAB_FOLLOW_PEOPLE
import timber.log.Timber

class MyFollowViewModel : BaseViewModel() {

    var lastTabIndex =0

    private val _deleteFollow = MutableLiveData<Int>()
    val deleteFollow: LiveData<Int> = _deleteFollow

    fun setDeleteNotify(){
        when(lastTabIndex){
            TAB_FOLLOW_PEOPLE->{
                _deleteFollow.value = lastTabIndex
            }
            else ->{
                _deleteFollow.value = lastTabIndex
            }
        }
    }

}
