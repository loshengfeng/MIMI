package com.dabenxiang.mimi.callback

interface PagingCallback {
    fun onLoading() {}
    fun onLoaded() {}
    fun onSucceed() {}
    fun onThrowable(throwable: Throwable) {}
    fun onTotalCount(count: Long) {}
    fun onCurrentItemCount(count: Long, isInitial: Boolean) {}
    fun onGetAny(obj: Any?) {}
}