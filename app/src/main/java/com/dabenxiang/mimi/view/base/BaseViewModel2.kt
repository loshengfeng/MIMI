package com.dabenxiang.mimi.view.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

abstract class BaseViewModel2 : BaseViewModel() {
    private val mProcessing by lazy { MutableLiveData<Boolean>() }
    val processing: LiveData<Boolean> get() = mProcessing

    private val mNavigateDestination by lazy { MutableLiveData<NavigateItem>() }
    val navigateDestination: LiveData<NavigateItem> = mNavigateDestination

    open fun navigateTo(item: NavigateItem) {
        viewModelScope.launch {
            navigationTaskJoinOrRun {
                mNavigateDestination.value = item
                delay(200L)
                mNavigateDestination.value = NavigateItem.Clean
                delay(1000L)
            }
        }
    }

    private var navigationTask: Deferred<Any>? = null

    private suspend fun navigationTaskJoinOrRun(block: suspend () -> Any): Any {
        navigationTask?.let {
            return it.await()
        }

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                navigationTask = null
            }

            navigationTask = newTask
            newTask.await()
        }
    }
}