package com.dabenxiang.mimi.callback

interface MyFollowPagingCallback : PagingCallback {
    fun onTotalCount(count: Long, isInitial: Boolean) {}
    fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {}
}