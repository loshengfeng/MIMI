package com.dabenxiang.mimi.view.base

import android.accounts.AccountManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val accountManager: AccountManager by inject()
    val gson: Gson by inject()
//    val toastData = MutableLiveData<String>()
//    val dialogData = MutableLiveData<String>()

    private val mShowProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = mShowProgress

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
