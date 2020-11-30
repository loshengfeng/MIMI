package com.dabenxiang.mimi.callback

interface MyLikePagingCallback : PagingCallback {
    fun onIdList(list: ArrayList<Long>, isInitial: Boolean)
}