package com.dabenxiang.mimi.callback

interface TopUpPagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onThrowable(throwable: Throwable)
    fun listEmpty(isEmpty: Boolean)
}