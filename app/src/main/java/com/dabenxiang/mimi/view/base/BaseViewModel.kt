package com.dabenxiang.mimi.view.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.google.gson.Gson
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val accountManager: AccountManager by inject()
    val gson: Gson by inject()
    val toastData = MutableLiveData<String>()

    private val _showProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = _showProgress

    fun setShowProgress(show: Boolean) {
        _showProgress.value = show
    }

    fun processException(exceptionResult: ExceptionResult) {
        // todo
//        when (exceptionResult) {
//            is ExceptionResult.Crash -> deviceManager.sendCrashReport(AppUtils.getExceptionDetail(exceptionResult.throwable))
//            is ExceptionResult.HttpError -> deviceManager.sendCrashReport(AppUtils.getExceptionDetail(exceptionResult.httpExceptionItem.httpExceptionClone))
//        }
    }
}
