package com.dabenxiang.mimi.callback

interface PagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onSucceed() {}
    fun onThrowable(throwable: Throwable)
}