package com.dabenxiang.mimi.view.home

interface PagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onThrowable(throwable: Throwable)
}