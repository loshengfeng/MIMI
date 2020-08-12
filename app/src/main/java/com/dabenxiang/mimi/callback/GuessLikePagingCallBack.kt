package com.dabenxiang.mimi.callback

interface GuessLikePagingCallBack : PagingCallback {
    fun onLoadInit(initCount: Int)
}