package com.dabenxiang.mimi.view.listener

import android.view.View

interface AdapterEventListener<T> {
    fun onItemClick(view: View, item: T)
}