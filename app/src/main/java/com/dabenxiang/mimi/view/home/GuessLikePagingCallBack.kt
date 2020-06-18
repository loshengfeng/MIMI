package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.callback.PagingCallback

interface GuessLikePagingCallBack : PagingCallback {
    fun onLoadInit(initCount: Int)
}