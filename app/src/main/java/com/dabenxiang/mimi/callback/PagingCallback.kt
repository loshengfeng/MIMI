package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.Category

interface PagingCallback {
    fun onLoading()
    fun onLoaded()
    fun onSucceed() {}
    fun onThrowable(throwable: Throwable)
    fun onTotalCount(count: Long) {}
    fun onGetCategory(category: Category?) {}
}