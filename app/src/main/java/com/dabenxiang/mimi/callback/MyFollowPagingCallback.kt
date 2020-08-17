package com.dabenxiang.mimi.callback

interface MyFollowPagingCallback : PagingCallback {
    fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {}
}