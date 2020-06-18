package com.dabenxiang.mimi.callback

interface PagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onThrowable(throwable: Throwable)
}