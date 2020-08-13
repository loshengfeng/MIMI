package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.Category

interface PagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onSucceed() {}
    fun onThrowable(throwable: Throwable)
    fun onTotalCount(count: Long) {}
    fun onTotalCount(count: Long, isInitial: Boolean) {}
    fun onGetCategory(category: Category?) {}
}