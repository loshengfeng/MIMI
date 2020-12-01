package com.dabenxiang.mimi.view.mycollection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import timber.log.Timber

class MyCollectionViewModel : BaseViewModel() {

    var lastTabIndex =0

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    private val _deleteMiMIs = MutableLiveData<Int>()
    val deleteMiMIs: LiveData<Int> = _deleteMiMIs

    fun setDeleteNotify(){
        when(lastTabIndex){
            MyCollectionFragment.TAB_FAVORITES ->{
                _deleteFavorites.value = lastTabIndex
            }
            else ->{
                _deleteMiMIs.value = lastTabIndex
            }
        }
    }

}
