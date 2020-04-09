package com.dabenxiang.mimi.view.base

import android.view.View

abstract class BaseAnyViewHolder<VM: Any>(itemView: View): BaseViewHolder(itemView) {

    protected var data: VM? = null

    fun bind(bind: VM?) {
        data = bind
        updated()
    }

    abstract fun updated()
}